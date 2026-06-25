package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Design
import com.example.data.model.DesignElement
import com.example.data.repository.DesignRepository
import com.example.utils.DesignTemplates
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class DesignViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DesignRepository
    val allDesigns: StateFlow<List<Design>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DesignRepository(database.designDao())
        allDesigns = repository.allDesigns.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, DesignElement::class.java)
    private val adapter = moshi.adapter<List<DesignElement>>(listType)

    // Editor state
    private val _activeDesign = MutableStateFlow<Design?>(null)
    val activeDesign: StateFlow<Design?> = _activeDesign.asStateFlow()

    private val _activeElements = MutableStateFlow<List<DesignElement>>(emptyList())
    val activeElements: StateFlow<List<DesignElement>> = _activeElements.asStateFlow()

    // Undo & Redo stacks
    private val undoStack = mutableListOf<List<DesignElement>>()
    private val redoStack = mutableListOf<List<DesignElement>>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    // Selected element inside editor
    private val _selectedElementId = MutableStateFlow<String?>(null)
    val selectedElementId: StateFlow<String?> = _selectedElementId.asStateFlow()

    // Utility to parse JSON elements
    private fun parseElements(json: String): List<DesignElement> {
        if (json.isEmpty()) return emptyList()
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Utility to format list to JSON elements
    private fun formatElements(elements: List<DesignElement>): String {
        return adapter.toJson(elements)
    }

    fun loadDesign(id: Int) {
        viewModelScope.launch {
            val design = repository.getDesignByIdSuspended(id)
            if (design != null) {
                _activeDesign.value = design
                val elements = parseElements(design.elementsJson)
                _activeElements.value = elements
                
                // Reset undo/redo
                undoStack.clear()
                redoStack.clear()
                _canUndo.value = false
                _canRedo.value = false
                _selectedElementId.value = null
            }
        }
    }

    fun closeActiveDesign() {
        _activeDesign.value = null
        _activeElements.value = emptyList()
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        _selectedElementId.value = null
    }

    // Capture the state BEFORE an edit action starts, so we can undo to it
    fun saveStateToUndoStack() {
        val currentElements = _activeElements.value.map { it.copy() }
        undoStack.add(currentElements)
        if (undoStack.size > 25) {
            undoStack.removeAt(0) // Limit stack size
        }
        redoStack.clear()
        _canUndo.value = true
        _canRedo.value = false
    }

    fun updateElements(newElements: List<DesignElement>, autoSave: Boolean = true) {
        _activeElements.value = newElements
        if (autoSave) {
            triggerAutoSave()
        }
    }

    fun selectElement(id: String?) {
        _selectedElementId.value = id
    }

    // Undo action
    fun undo() {
        if (undoStack.isNotEmpty()) {
            val currentElements = _activeElements.value.map { it.copy() }
            redoStack.add(currentElements)
            _canRedo.value = true

            val previousElements = undoStack.removeAt(undoStack.size - 1)
            _activeElements.value = previousElements
            _canUndo.value = undoStack.isNotEmpty()

            triggerAutoSave()
        }
    }

    // Redo action
    fun redo() {
        if (redoStack.isNotEmpty()) {
            val currentElements = _activeElements.value.map { it.copy() }
            undoStack.add(currentElements)
            _canUndo.value = true

            val nextElements = redoStack.removeAt(redoStack.size - 1)
            _activeElements.value = nextElements
            _canRedo.value = redoStack.isNotEmpty()

            triggerAutoSave()
        }
    }

    // Update background color, gradient, image, or name
    fun updateDesignProperties(
        name: String? = null,
        backgroundType: String? = null,
        backgroundColor: Int? = null,
        backgroundGradientStart: Int? = null,
        backgroundGradientEnd: Int? = null,
        backgroundImageUri: String? = null
    ) {
        val current = _activeDesign.value ?: return
        val updated = current.copy(
            name = name ?: current.name,
            backgroundType = backgroundType ?: current.backgroundType,
            backgroundColor = backgroundColor ?: current.backgroundColor,
            backgroundGradientStart = backgroundGradientStart ?: current.backgroundGradientStart,
            backgroundGradientEnd = backgroundGradientEnd ?: current.backgroundGradientEnd,
            backgroundImageUri = if (backgroundType == "IMAGE") backgroundImageUri else current.backgroundImageUri,
            updatedAt = System.currentTimeMillis()
        )
        _activeDesign.value = updated
        
        viewModelScope.launch {
            repository.updateDesign(updated)
        }
    }

    private fun triggerAutoSave() {
        val current = _activeDesign.value ?: return
        val elementsJson = formatElements(_activeElements.value)
        val updated = current.copy(
            elementsJson = elementsJson,
            updatedAt = System.currentTimeMillis()
        )
        _activeDesign.value = updated
        viewModelScope.launch {
            repository.updateDesign(updated)
        }
    }

    // Create a new design project with preset elements if templates exist
    fun createNewDesign(category: String, name: String) {
        viewModelScope.launch {
            val preset = DesignTemplates.categories.firstOrNull { it.name == category }
            val width = preset?.width ?: 1080f
            val height = preset?.height ?: 1080f
            
            // Random default palette
            val palette = DesignTemplates.getRandomPalette()
            val elements = DesignTemplates.createTemplateElements(category, width, height, palette)
            val jsonElements = formatElements(elements)

            val design = Design(
                name = name,
                category = category,
                width = width,
                height = height,
                backgroundType = if (category == "Business Card" || category == "YouTube Thumbnail") "SOLID" else "GRADIENT",
                backgroundColor = if (category == "Business Card") palette.background else 0xFFFFFFFF.toInt(),
                backgroundGradientStart = palette.primary,
                backgroundGradientEnd = palette.accent,
                elementsJson = jsonElements,
                updatedAt = System.currentTimeMillis()
            )

            val generatedId = repository.insertDesign(design)
            loadDesign(generatedId.toInt())
        }
    }

    fun deleteDesign(id: Int) {
        viewModelScope.launch {
            repository.deleteDesignById(id)
            if (_activeDesign.value?.id == id) {
                closeActiveDesign()
            }
        }
    }

    fun duplicateDesign(design: Design) {
        viewModelScope.launch {
            val duplicated = design.copy(
                id = 0, // Reset id to autoGenerate
                name = "${design.name} (Copy)",
                updatedAt = System.currentTimeMillis()
            )
            repository.insertDesign(duplicated)
        }
    }
}
