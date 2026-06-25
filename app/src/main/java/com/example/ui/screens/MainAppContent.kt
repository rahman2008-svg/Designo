@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Design
import com.example.data.model.DesignElement
import com.example.ui.components.StickerShape
import com.example.ui.viewmodel.DesignViewModel
import com.example.utils.DesignExporter
import com.example.utils.DesignTemplates
import java.util.UUID

// Convert raw signed integer color values safely to Compose Color
private fun toColor(value: Int): Color {
    return Color(value.toLong() and 0xFFFFFFFFL)
}

@Composable
fun MainAppContent(viewModel: DesignViewModel) {
    val activeDesign by viewModel.activeDesign.collectAsStateWithLifecycle()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (activeDesign == null) {
            DashboardScreen(viewModel)
        } else {
            EditorScreen(viewModel)
        }
    }
}

@Composable
fun DashboardScreen(viewModel: DesignViewModel) {
    val context = LocalContext.current
    val designs by viewModel.allDesigns.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedCategoryForNew by remember { mutableStateOf("Instagram Post") }
    var newProjectName by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_designo_logo),
                            contentDescription = "Designo Logo",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Designo",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newProjectName = "My Draft Design"
                    showCreateDialog = true
                },
                modifier = Modifier.testTag("create_design_fab"),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New Design")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Preset Templates Carousel
            Text(
                text = "Start with a Preset Template",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DesignTemplates.categories) { category ->
                    Card(
                        modifier = Modifier
                            .width(140.dp)
                            .clickable {
                                viewModel.createNewDesign(category.name, "My ${category.name}")
                            }
                            .testTag("preset_${category.name.replace(" ", "_")}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = category.icon, fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${category.width.toInt()}x${category.height.toInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Saved Designs Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Saved Designs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${designs.size} Projects",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            if (designs.isEmpty()) {
                // Beautiful Empty State
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArtTrack,
                            contentDescription = "Empty",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No designs created yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the '+' button or select a template above to start designing offline!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Grid of saved designs
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    designs.forEach { design ->
                        SavedDesignRow(
                            design = design,
                            onClick = { viewModel.loadDesign(design.id) },
                            onDelete = { viewModel.deleteDesign(design.id) },
                            onDuplicate = { viewModel.duplicateDesign(design) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Developer and Company Info
            DeveloperCreditsSection()
        }
    }
    
    // Create Custom Project Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Design Project") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
                        label = { Text("Design Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select Canvas Ratio & Format:")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        var expandedCategory by remember { mutableStateOf(false) }
                        OutlinedButton(
                            onClick = { expandedCategory = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedCategoryForNew)
                                Icon(Icons.Filled.ArrowDropDown, "Select Format")
                            }
                        }
                        DropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            DesignTemplates.categories.forEach { preset ->
                                DropdownMenuItem(
                                    text = { Text("${preset.icon} ${preset.name} (${preset.width.toInt()}x${preset.height.toInt()})") },
                                    onClick = {
                                        selectedCategoryForNew = preset.name
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = if (newProjectName.isBlank()) "My Offline Design" else newProjectName
                        viewModel.createNewDesign(selectedCategoryForNew, name)
                        showCreateDialog = false
                    },
                    modifier = Modifier.testTag("confirm_create_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SavedDesignRow(
    design: Design,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("saved_design_${design.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(toColor(design.backgroundColor), RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ColorLens,
                    contentDescription = null,
                    tint = if (toColor(design.backgroundColor).luminance() > 0.5) Color.DarkGray else Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = design.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = design.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = " • ${design.width.toInt()} x ${design.height.toInt()} px",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.MoreVert, "Actions")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        leadingIcon = { Icon(Icons.Filled.ContentCopy, null) },
                        onClick = {
                            onDuplicate()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Filled.Delete, null) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DeveloperCreditsSection() {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "👨💻 About the Developer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Prince AR Abdur Rahman",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "An independent app developer passionate about crafting highly functional offline-first tools, sleek user interfaces, and next-generation mobile products.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/share/1BNn32qoJo/"))
                        context.startActivity(intent)
                    },
                    label = { Text("Facebook") },
                    leadingIcon = { Icon(Icons.Filled.Link, null, modifier = Modifier.size(16.dp)) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                AssistChip(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/ur___abdur____rahman__2008"))
                        context.startActivity(intent)
                    },
                    label = { Text("Instagram") },
                    leadingIcon = { Icon(Icons.Filled.Link, null, modifier = Modifier.size(16.dp)) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "📞 Contact on WhatsApp:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "01707424006 | 01796951709",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Numbers copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "🏢 NexVora Lab's Ofc",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Privacy-friendly, lightning-fast productivity applications.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "© 2026 NexVora Lab's Ofc. All Rights Reserved.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun EditorScreen(viewModel: DesignViewModel) {
    val context = LocalContext.current
    val design by viewModel.activeDesign.collectAsStateWithLifecycle()
    val elements by viewModel.activeElements.collectAsStateWithLifecycle()
    val selectedId by viewModel.selectedElementId.collectAsStateWithLifecycle()
    
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    
    var showExportMenu by remember { mutableStateOf(false) }
    var useHdExport by remember { mutableStateOf(true) }
    
    var currentTab by remember { mutableStateOf("layers") } // "add", "layers", "edit", "bg"
    
    if (design == null) return
    val activeDesignNonNull = design!!
    
    val selectedElement = elements.find { it.id == selectedId }

    // Image/Background selectors
    val backgroundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.updateDesignProperties(
                backgroundType = "IMAGE",
                backgroundImageUri = uri.toString()
            )
        }
    }
    
    val layerImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.saveStateToUndoStack()
            val newElement = DesignElement(
                id = UUID.randomUUID().toString(),
                type = "IMAGE",
                x = 100f,
                y = 100f,
                width = 300f,
                height = 300f,
                imageUri = uri.toString()
            )
            viewModel.updateElements(elements + newElement)
            viewModel.selectElement(newElement.id)
            currentTab = "edit"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    BasicTextField(
                        value = activeDesignNonNull.name,
                        onValueChange = { viewModel.updateDesignProperties(name = it) },
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth(0.6f)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.closeActiveDesign() },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close Editor")
                    }
                },
                actions = {
                    // Undo
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = canUndo,
                        modifier = Modifier.testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                    
                    // Redo
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = canRedo,
                        modifier = Modifier.testTag("redo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                    
                    // Export Menu
                    Box {
                        IconButton(
                            onClick = { showExportMenu = true },
                            modifier = Modifier.testTag("export_button")
                        ) {
                            Icon(Icons.Filled.SaveAlt, contentDescription = "Export")
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export HD PNG (High Res)") },
                                leadingIcon = { Icon(Icons.Filled.Image, null) },
                                onClick = {
                                    showExportMenu = false
                                    DesignExporter.exportToGallery(
                                        context, activeDesignNonNull, elements, "PNG", if (useHdExport) 3.0f else 1.5f
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export HD JPG") },
                                leadingIcon = { Icon(Icons.Filled.Photo, null) },
                                onClick = {
                                    showExportMenu = false
                                    DesignExporter.exportToGallery(
                                        context, activeDesignNonNull, elements, "JPG", if (useHdExport) 3.0f else 1.5f
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export PDF File") },
                                leadingIcon = { Icon(Icons.Filled.PictureAsPdf, null) },
                                onClick = {
                                    showExportMenu = false
                                    DesignExporter.exportToPDF(
                                        context, activeDesignNonNull, elements
                                    )
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = useHdExport, onCheckedChange = { useHdExport = it })
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Enable 3x HD Render")
                                    }
                                },
                                onClick = { useHdExport = !useHdExport }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Workspace Canvas Area (Takes up top half)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .clickable { viewModel.selectElement(null) }, // Tap off elements to deselect
                contentAlignment = Alignment.Center
            ) {
                EditorCanvasWorkspace(
                    design = activeDesignNonNull,
                    elements = elements,
                    selectedId = selectedId,
                    onElementsUpdated = { viewModel.updateElements(it, true) },
                    onSelectElement = { viewModel.selectElement(it) },
                    onSaveUndoState = { viewModel.saveStateToUndoStack() }
                )
            }
            
            // Property sheets and addition tabs (Bottom half)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp),
                tonalElevation = 6.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column {
                    // Control category selectors
                    TabRow(
                        selectedTabIndex = when (currentTab) {
                            "add" -> 0
                            "layers" -> 1
                            "bg" -> 2
                            "edit" -> 3
                            else -> 1
                        }
                    ) {
                        Tab(
                            selected = currentTab == "add",
                            onClick = { currentTab = "add" },
                            text = { Row { Icon(Icons.Filled.AddCircle, null); Spacer(modifier = Modifier.width(4.dp)); Text("Add") } }
                        )
                        Tab(
                            selected = currentTab == "layers",
                            onClick = { currentTab = "layers" },
                            text = { Row { Icon(Icons.Filled.Layers, null); Spacer(modifier = Modifier.width(4.dp)); Text("Layers") } }
                        )
                        Tab(
                            selected = currentTab == "bg",
                            onClick = { currentTab = "bg" },
                            text = { Row { Icon(Icons.Filled.Wallpaper, null); Spacer(modifier = Modifier.width(4.dp)); Text("Backdrop") } }
                        )
                        Tab(
                            selected = currentTab == "edit",
                            onClick = { currentTab = "edit" },
                            text = { Row { Icon(Icons.Filled.Edit, null); Spacer(modifier = Modifier.width(4.dp)); Text("Styling") } }
                        )
                    }
                    
                    // Specific Panel depending on active tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        when (currentTab) {
                            "add" -> AddElementsPanel(
                                elements = elements,
                                onAddElement = { 
                                    viewModel.saveStateToUndoStack()
                                    viewModel.updateElements(elements + it)
                                    viewModel.selectElement(it.id)
                                    currentTab = "edit"
                                },
                                onImageImportClick = { layerImageLauncher.launch("image/*") }
                            )
                            "layers" -> LayersPanel(
                                elements = elements,
                                selectedId = selectedId,
                                onSelectElement = { viewModel.selectElement(it) },
                                onMoveLayer = { index, targetIndex ->
                                    viewModel.saveStateToUndoStack()
                                    val newList = elements.toMutableList()
                                    val item = newList.removeAt(index)
                                    newList.add(targetIndex, item)
                                    viewModel.updateElements(newList)
                                },
                                onDeleteLayer = { 
                                    viewModel.saveStateToUndoStack()
                                    viewModel.updateElements(elements.filter { el -> el.id != it })
                                    viewModel.selectElement(null)
                                },
                                onToggleLock = { id ->
                                    viewModel.saveStateToUndoStack()
                                    viewModel.updateElements(elements.map { el ->
                                        if (el.id == id) el.copy(isLocked = !el.isLocked) else el
                                    })
                                }
                            )
                            "bg" -> BackgroundPanel(
                                design = activeDesignNonNull,
                                onUpdateBg = { type, color, start, end ->
                                    viewModel.updateDesignProperties(
                                        backgroundType = type,
                                        backgroundColor = color,
                                        backgroundGradientStart = start,
                                        backgroundGradientEnd = end
                                    )
                                },
                                onImageBgClick = { backgroundLauncher.launch("image/*") }
                            )
                            "edit" -> EditPropertiesPanel(
                                selectedElement = selectedElement,
                                onUpdateElement = { updated ->
                                    // Trigger position/properties save
                                    viewModel.updateElements(elements.map { if (it.id == updated.id) updated else it })
                                },
                                onSaveUndoState = { viewModel.saveStateToUndoStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Checkered canvas background representing transparent checkerboard
@Composable
fun EditorCanvasWorkspace(
    design: Design,
    elements: List<DesignElement>,
    selectedId: String?,
    onElementsUpdated: (List<DesignElement>) -> Unit,
    onSelectElement: (String?) -> Unit,
    onSaveUndoState: () -> Unit
) {
    val context = LocalContext.current
    
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Calculate dynamic aspect ratio to fit the view constraints perfectly
        val viewW = maxWidth.value
        val viewH = maxHeight.value
        val designRatio = design.width / design.height
        val containerRatio = viewW / viewH

        val finalW: Float
        val finalH: Float
        if (designRatio > containerRatio) {
            finalW = viewW
            finalH = viewW / designRatio
        } else {
            finalH = viewH
            finalW = viewH * designRatio
        }

        val scaleFactor = finalW / design.width

        // Outer transparent canvas box
        Box(
            modifier = Modifier
                .size(width = finalW.dp, height = finalH.dp)
                .testTag("design_canvas")
                .drawBehind {
                    // Draw checkered background for transparency previewing
                    val sizeX = 15.dp.toPx()
                    val cols = (size.width / sizeX).toInt() + 1
                    val rows = (size.height / sizeX).toInt() + 1
                    for (c in 0 until cols) {
                        for (r in 0 until rows) {
                            val color = if ((c + r) % 2 == 0) Color(0xFFE2E8F0) else Color.White
                            drawRect(
                                color = color,
                                topLeft = Offset(c * sizeX, r * sizeX),
                                size = androidx.compose.ui.geometry.Size(sizeX, sizeX)
                            )
                        }
                    }
                }
                .background(
                    when (design.backgroundType) {
                        "SOLID" -> SolidColorBrush(toColor(design.backgroundColor))
                        "GRADIENT" -> Brush.verticalGradient(
                            listOf(toColor(design.backgroundGradientStart), toColor(design.backgroundGradientEnd))
                        )
                        else -> SolidColorBrush(Color.Transparent)
                    }
                )
        ) {
            // Draw image background if selected
            if (design.backgroundType == "IMAGE" && !design.backgroundImageUri.isNullOrEmpty()) {
                AsyncImage(
                    model = design.backgroundImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Draw all design layers
            elements.forEach { element ->
                val elW = (element.width * scaleFactor).dp
                val elH = (element.height * scaleFactor).dp
                val isSelected = element.id == selectedId

                // Render alignment snapping guides during active move
                // Smart guideline helper (rule-based)
                Box(
                    modifier = Modifier
                        .offset(
                            x = (element.x * scaleFactor).dp,
                            y = (element.y * scaleFactor).dp
                        )
                        .size(width = elW, height = elH)
                        .rotate(element.rotation)
                        // Element Dragging gestures & snapping helper
                        .pointerInput(element.id) {
                            detectDragGestures(
                                onDragStart = {
                                    if (!element.isLocked) {
                                        onSaveUndoState()
                                        onSelectElement(element.id)
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    if (!element.isLocked) {
                                        change.consume()
                                        
                                        // Standard coordinates movement
                                        var newX = element.x + dragAmount.x / scaleFactor
                                        var newY = element.y + dragAmount.y / scaleFactor
                                        
                                        // Smart alignment and snap-to-grid rule-based helper (No AI)
                                        val cCenterX = design.width / 2f
                                        val cCenterY = design.height / 2f
                                        val elCenterX = newX + element.width / 2f
                                        val elCenterY = newY + element.height / 2f
                                        val snapThreshold = 18f
                                        
                                        // Vertical snapping (Center-X & Edges)
                                        if (Math.abs(elCenterX - cCenterX) < snapThreshold) {
                                            newX = cCenterX - element.width / 2f
                                        } else if (Math.abs(newX) < snapThreshold) {
                                            newX = 0f
                                        } else if (Math.abs(newX + element.width - design.width) < snapThreshold) {
                                            newX = design.width - element.width
                                        }
                                        
                                        // Horizontal snapping (Center-Y & Edges)
                                        if (Math.abs(elCenterY - cCenterY) < snapThreshold) {
                                            newY = cCenterY - element.height / 2f
                                        } else if (Math.abs(newY) < snapThreshold) {
                                            newY = 0f
                                        } else if (Math.abs(newY + element.height - design.height) < snapThreshold) {
                                            newY = design.height - element.height
                                        }
                                        
                                        // Update element coordinates
                                        val updatedList = elements.map {
                                            if (it.id == element.id) it.copy(x = newX, y = newY) else it
                                        }
                                        onElementsUpdated(updatedList)
                                    }
                                }
                            )
                        }
                        .combinedClickable(
                            onClick = { onSelectElement(element.id) },
                            onDoubleClick = { if (element.isLocked) onSelectElement(element.id) }
                        )
                        // Selection frame border
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (element.isLocked) Color.Red else MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(2.dp)
                        )
                ) {
                    // Element specific inner composables
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (element.type) {
                            "TEXT" -> {
                                Text(
                                    text = element.text,
                                    fontSize = (element.fontSize * scaleFactor).sp,
                                    color = toColor(element.textColor),
                                    fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
                                    fontStyle = if (element.isItalic) FontStyle.Italic else FontStyle.Normal,
                                    textDecoration = if (element.isUnderline) TextDecoration.Underline else TextDecoration.None,
                                    textAlign = when (element.alignment) {
                                        "LEFT" -> TextAlign.Left
                                        "RIGHT" -> TextAlign.Right
                                        else -> TextAlign.Center
                                    },
                                    fontFamily = when (element.fontFamily) {
                                        "Serif" -> FontFamily.Serif
                                        "Monospace" -> FontFamily.Monospace
                                        "Cursive" -> FontFamily.Cursive
                                        else -> FontFamily.SansSerif
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                    lineHeight = (element.fontSize * scaleFactor * 1.2f).sp
                                )
                            }
                            "SHAPE" -> {
                                when (element.shapeType) {
                                    "CIRCLE" -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    toColor(element.fillColor),
                                                    shape = CircleShape
                                                )
                                                .border(
                                                    (element.shapeStrokeWidth * scaleFactor).dp,
                                                    toColor(element.shapeStrokeColor),
                                                    CircleShape
                                                )
                                        )
                                    }
                                    "SQUARE" -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(toColor(element.fillColor))
                                                .border(
                                                    (element.shapeStrokeWidth * scaleFactor).dp,
                                                    toColor(element.shapeStrokeColor)
                                                )
                                        )
                                    }
                                    "ARROW" -> {
                                        StickerShape(
                                            stickerName = "Arrow",
                                            color = toColor(element.fillColor),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    "LINE" -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height((element.shapeStrokeWidth * scaleFactor).dp)
                                                .align(Alignment.Center)
                                                .background(toColor(element.fillColor))
                                        )
                                    }
                                }
                            }
                            "STICKER" -> {
                                StickerShape(
                                    stickerName = element.stickerName,
                                    color = toColor(element.fillColor),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            "IMAGE" -> {
                                AsyncImage(
                                    model = element.imageUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        
                        // Small lock indicator
                        if (element.isLocked) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Locked",
                                tint = Color.Red,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(4.dp)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                    
                    // Show active resize controls only on bottom right if selected and not locked
                    if (isSelected && !element.isLocked) {
                        // Bottom-Right Resize Handle
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.BottomEnd)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                                .pointerInput(element.id) {
                                    detectDragGestures(
                                        onDragStart = { onSaveUndoState() },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val newW = Math.max(40f, element.width + dragAmount.x / scaleFactor)
                                            val newH = Math.max(40f, element.height + dragAmount.y / scaleFactor)
                                            
                                            val updatedList = elements.map {
                                                if (it.id == element.id) it.copy(width = newW, height = newH) else it
                                            }
                                            onElementsUpdated(updatedList)
                                        }
                                    )
                                }
                        )
                        
                        // Top Center Rotation Handle
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = (-16).dp)
                                .background(Color.Yellow, CircleShape)
                                .border(2.dp, Color.Black, CircleShape)
                                .pointerInput(element.id) {
                                    detectDragGestures(
                                        onDragStart = { onSaveUndoState() },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            // Dynamic angular rotation rotation math
                                            val elCX = element.x + element.width / 2f
                                            val elCY = element.y + element.height / 2f
                                            val touchPointX = element.x + (change.position.x / scaleFactor)
                                            val touchPointY = element.y + (change.position.y / scaleFactor)
                                            
                                            val rad = Math.atan2(
                                                (touchPointY - elCY).toDouble(),
                                                (touchPointX - elCX).toDouble()
                                            )
                                            var deg = Math.toDegrees(rad).toFloat() + 90f
                                            if (deg < 0) deg += 360f
                                            
                                            // Snap to 45 degree multiples if close
                                            if (Math.abs(deg % 45) < 8) {
                                                deg = (Math.round(deg / 45f) * 45).toFloat()
                                            }
                                            
                                            val updatedList = elements.map {
                                                if (it.id == element.id) it.copy(rotation = deg) else it
                                            }
                                            onElementsUpdated(updatedList)
                                        }
                                    )
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Autorenew,
                                contentDescription = "Rotate",
                                modifier = Modifier
                                    .size(14.dp)
                                    .align(Alignment.Center),
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

// Solid color brush helper
private fun SolidColorBrush(color: Color): Brush {
    return Brush.horizontalGradient(listOf(color, color))
}

@Composable
fun AddElementsPanel(
    elements: List<DesignElement>,
    onAddElement: (DesignElement) -> Unit,
    onImageImportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Select layer type to insert:", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    onAddElement(
                        DesignElement(
                            id = UUID.randomUUID().toString(),
                            type = "TEXT",
                            x = 200f, y = 200f,
                            width = 500f, height = 120f,
                            text = "A Beautiful Heading",
                            fontSize = 32f,
                            textColor = 0xFF000000.toInt()
                        )
                    )
                },
                modifier = Modifier.weight(1f).testTag("add_text_button")
            ) {
                Icon(Icons.Filled.TextFields, null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Text")
            }
            
            Button(
                onClick = onImageImportClick,
                modifier = Modifier.weight(1f).testTag("add_image_button")
            ) {
                Icon(Icons.Filled.AddPhotoAlternate, null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Gallery Image")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Shapes Pack:", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("SQUARE", "CIRCLE", "LINE").forEach { shape ->
                OutlinedButton(
                    onClick = {
                        onAddElement(
                            DesignElement(
                                id = UUID.randomUUID().toString(),
                                type = "SHAPE",
                                x = 300f, y = 300f,
                                width = 200f, height = 200f,
                                shapeType = shape,
                                fillColor = 0xFF3B82F6.toInt()
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(shape)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Stickers (Offline Library):", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(DesignTemplates.stickers) { sticker ->
                Card(
                    modifier = Modifier
                        .size(80.dp)
                        .clickable {
                            onAddElement(
                                DesignElement(
                                    id = UUID.randomUUID().toString(),
                                    type = "STICKER",
                                    x = 350f, y = 350f,
                                    width = 150f, height = 150f,
                                    stickerName = sticker,
                                    fillColor = 0xFFF59E0B.toInt()
                                )
                            )
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        StickerShape(
                            stickerName = sticker,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(sticker, fontSize = 10.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun LayersPanel(
    elements: List<DesignElement>,
    selectedId: String?,
    onSelectElement: (String?) -> Unit,
    onMoveLayer: (index: Int, targetIndex: Int) -> Unit,
    onDeleteLayer: (String) -> Unit,
    onToggleLock: (String) -> Unit
) {
    if (elements.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No layers added. Tap 'Add' to insert elements.")
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Manage Design Layers (order bottom to top):", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Display elements in reversed order since the last item in the list is the top-most layer
            elements.asReversed().forEachIndexed { reversedIdx, element ->
                val actualIdx = elements.size - 1 - reversedIdx
                val isSelected = element.id == selectedId
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelectElement(element.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (element.type) {
                                "TEXT" -> Icons.Filled.TextFields
                                "SHAPE" -> Icons.Filled.Category
                                "STICKER" -> Icons.Filled.Star
                                else -> Icons.Filled.Image
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (element.type) {
                                    "TEXT" -> "Text: \"${element.text.take(15)}\""
                                    "SHAPE" -> "Shape: ${element.shapeType}"
                                    "STICKER" -> "Sticker: ${element.stickerName}"
                                    else -> "Layer Image"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Lock Toggle
                        IconButton(onClick = { onToggleLock(element.id) }) {
                            Icon(
                                imageVector = if (element.isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                contentDescription = "Lock Layer",
                                tint = if (element.isLocked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Order Layer buttons
                        IconButton(
                            onClick = { onMoveLayer(actualIdx, actualIdx + 1) },
                            enabled = actualIdx < elements.size - 1
                        ) {
                            Icon(Icons.Filled.ArrowUpward, "Bring Forward", modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { onMoveLayer(actualIdx, actualIdx - 1) },
                            enabled = actualIdx > 0
                        ) {
                            Icon(Icons.Filled.ArrowDownward, "Send Backward", modifier = Modifier.size(18.dp))
                        }
                        
                        // Delete Button
                        IconButton(onClick = { onDeleteLayer(element.id) }) {
                            Icon(Icons.Filled.Delete, "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BackgroundPanel(
    design: Design,
    onUpdateBg: (type: String, color: Int, start: Int, end: Int) -> Unit,
    onImageBgClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Background Style:", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onUpdateBg("SOLID", 0xFFFFFFFF.toInt(), 0, 0) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (design.backgroundType == "SOLID") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Solid")
            }
            Button(
                onClick = { onUpdateBg("GRADIENT", 0, 0xFF3B82F6.toInt(), 0xFF8B5CF6.toInt()) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (design.backgroundType == "GRADIENT") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Gradient")
            }
            Button(
                onClick = onImageBgClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (design.backgroundType == "IMAGE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Backdrop")
            }
        }
        
        if (design.backgroundType == "SOLID") {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Select Solid Color:")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0xFFFFFFFF, 0xFF000000, 0xFFEF4444, 0xFF3B82F6, 0xFF10B981, 0xFFF59E0B, 0xFF8B5CF6, 0xFFEC4899).forEach { colorHex ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(toColor(colorHex.toInt()), CircleShape)
                            .border(
                                width = if (design.backgroundColor == colorHex.toInt()) 3.dp else 1.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .clickable { onUpdateBg("SOLID", colorHex.toInt(), 0, 0) }
                    )
                }
            }
        } else if (design.backgroundType == "GRADIENT") {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Select Preset Gradient:")
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Pair(0xFF6366F1, 0xFFA855F7), // Indigo -> Purple
                    Pair(0xFF06B6D4, 0xFF3B82F6), // Cyan -> Blue
                    Pair(0xFFF97316, 0xFFE11D48), // Orange -> Red
                    Pair(0xFF10B981, 0xFF059669)  // Mint -> Emerald
                ).forEach { pair ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(35.dp)
                            .background(
                                Brush.horizontalGradient(listOf(toColor(pair.first.toInt()), toColor(pair.second.toInt()))),
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = if (design.backgroundGradientStart == pair.first.toInt()) 2.dp else 0.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onUpdateBg(
                                    "GRADIENT",
                                    0,
                                    pair.first.toInt(),
                                    pair.second.toInt()
                                )
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun EditPropertiesPanel(
    selectedElement: DesignElement?,
    onUpdateElement: (DesignElement) -> Unit,
    onSaveUndoState: () -> Unit
) {
    if (selectedElement == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Select any layer on the canvas to edit its properties.", textAlign = TextAlign.Center)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Layer Options (${selectedElement.type})",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (selectedElement.isLocked) "Locked 🔒" else "Editable 🔓",
                style = MaterialTheme.typography.bodySmall,
                color = if (selectedElement.isLocked) Color.Red else Color.Green
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        if (selectedElement.isLocked) {
            Text("This layer is currently locked. Go to the 'Layers' tab to unlock it.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
            return
        }

        when (selectedElement.type) {
            "TEXT" -> {
                // Text editor field
                OutlinedTextField(
                    value = selectedElement.text,
                    onValueChange = {
                        onUpdateElement(selectedElement.copy(text = it))
                    },
                    label = { Text("Edit Content") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Font Size Slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Font Size: ${selectedElement.fontSize.toInt()}")
                    Slider(
                        value = selectedElement.fontSize,
                        onValueChange = {
                            onUpdateElement(selectedElement.copy(fontSize = it))
                        },
                        valueRange = 10f..120f,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Bold/Italic/Underline Toggles
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedElement.isBold,
                        onClick = {
                            onSaveUndoState()
                            onUpdateElement(selectedElement.copy(isBold = !selectedElement.isBold))
                        },
                        label = { Text("Bold") }
                    )
                    FilterChip(
                        selected = selectedElement.isItalic,
                        onClick = {
                            onSaveUndoState()
                            onUpdateElement(selectedElement.copy(isItalic = !selectedElement.isItalic))
                        },
                        label = { Text("Italic") }
                    )
                    FilterChip(
                        selected = selectedElement.isUnderline,
                        onClick = {
                            onSaveUndoState()
                            onUpdateElement(selectedElement.copy(isUnderline = !selectedElement.isUnderline))
                        },
                        label = { Text("Underline") }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Text Alignments
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("LEFT", "CENTER", "RIGHT").forEach { align ->
                        InputChip(
                            selected = selectedElement.alignment == align,
                            onClick = {
                                onSaveUndoState()
                                onUpdateElement(selectedElement.copy(alignment = align))
                            },
                            label = { Text(align) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Font Pairing (predefined pairs)
                Text("Predefined Font Pairings:")
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DesignTemplates.fontPairings) { pair ->
                        Card(
                            modifier = Modifier
                                .width(130.dp)
                                .clickable {
                                    onSaveUndoState()
                                    onUpdateElement(selectedElement.copy(fontFamily = pair.titleFamily))
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedElement.fontFamily == pair.titleFamily) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(pair.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(pair.titleFamily, fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                // Font Colors
                Text("Text Color:")
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0xFF000000, 0xFFFFFFFF, 0xFF3B82F6, 0xFFEF4444, 0xFF10B981, 0xFFF59E0B, 0xFF8B5CF6, 0xFFEC4899).forEach { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(toColor(colorHex.toInt()), CircleShape)
                                .border(
                                    width = if (selectedElement.textColor == colorHex.toInt()) 3.dp else 1.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable {
                                    onSaveUndoState()
                                    onUpdateElement(selectedElement.copy(textColor = colorHex.toInt()))
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                // Shadow Slider (Stroke & depth)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Shadow Radius: ${selectedElement.shadowRadius.toInt()}")
                    Slider(
                        value = selectedElement.shadowRadius,
                        onValueChange = {
                            onUpdateElement(selectedElement.copy(shadowRadius = it))
                        },
                        valueRange = 0f..25f,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            "SHAPE" -> {
                Text("Fill Shape Color:")
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0xFF3B82F6, 0xFFEF4444, 0xFF10B981, 0xFFF59E0B, 0xFF8B5CF6, 0xFFEC4899, 0xFF000000, 0xFFFFFFFF).forEach { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(toColor(colorHex.toInt()), CircleShape)
                                .border(
                                    width = if (selectedElement.fillColor == colorHex.toInt()) 3.dp else 1.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable {
                                    onSaveUndoState()
                                    onUpdateElement(selectedElement.copy(fillColor = colorHex.toInt()))
                                }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Stroke Width Slider
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Stroke Width: ${selectedElement.shapeStrokeWidth.toInt()}")
                    Slider(
                        value = selectedElement.shapeStrokeWidth,
                        onValueChange = {
                            onUpdateElement(selectedElement.copy(shapeStrokeWidth = it))
                        },
                        valueRange = 0f..20f,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Stroke Color:")
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0xFFFFFFFF, 0xFF000000, 0xFFEF4444, 0xFF3B82F6, 0xFF10B981, 0xFFF59E0B).forEach { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(toColor(colorHex.toInt()), CircleShape)
                                .border(
                                    width = if (selectedElement.shapeStrokeColor == colorHex.toInt()) 3.dp else 1.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable {
                                    onSaveUndoState()
                                    onUpdateElement(selectedElement.copy(shapeStrokeColor = colorHex.toInt()))
                                }
                        )
                    }
                }
            }
            
            "STICKER" -> {
                Text("Tint Sticker Color:")
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0xFFF59E0B, 0xFFEF4444, 0xFF3B82F6, 0xFF10B981, 0xFF8B5CF6, 0xFFFFFFFF, 0xFF000000).forEach { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(toColor(colorHex.toInt()), CircleShape)
                                .border(
                                    width = if (selectedElement.fillColor == colorHex.toInt()) 3.dp else 1.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable {
                                    onSaveUndoState()
                                    onUpdateElement(selectedElement.copy(fillColor = colorHex.toInt()))
                                }
                        )
                    }
                }
            }
            
            "IMAGE" -> {
                // Background remover panel
                Text("Smart Background Remover (Offline):", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Select target background color to transparent:", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Pair("Solid White", 0xFFFFFFFF.toInt()),
                        Pair("Solid Black", 0xFF000000.toInt()),
                        Pair("Chroma Green", 0xFF00FF00.toInt())
                    ).forEach { pair ->
                        FilterChip(
                            selected = selectedElement.removeBgColorRange == pair.second,
                            onClick = {
                                onSaveUndoState()
                                val target = if (selectedElement.removeBgColorRange == pair.second) null else pair.second
                                onUpdateElement(selectedElement.copy(removeBgColorRange = target))
                            },
                            label = { Text(pair.first) }
                        )
                    }
                }
                
                if (selectedElement.removeBgColorRange != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Chroma Tolerance: ${(selectedElement.removeBgTolerance * 100).toInt()}%")
                        Slider(
                            value = selectedElement.removeBgTolerance,
                            onValueChange = {
                                onUpdateElement(selectedElement.copy(removeBgTolerance = it))
                            },
                            valueRange = 0.01f..0.8f,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
