package produkcja.meble.mobile.ui.products

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import produkcja.meble.mobile.data.CreateProductRequest
import produkcja.meble.mobile.data.FurnitureType
import produkcja.meble.mobile.data.Product
import produkcja.meble.mobile.ui.common.resolveImageUrl
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    initialProduct: Product? = null,
    onDismiss: () -> Unit,
    onSave: (
        request: CreateProductRequest,
        imageBytes: ByteArray?,
        fileName: String?,
        mimeType: String?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) -> Unit,
) {
    val context = LocalContext.current
    val isEditing = initialProduct != null

    var model by rememberSaveable { mutableStateOf(initialProduct?.model.orEmpty()) }
    var selectedType by rememberSaveable {
        mutableStateOf(
            initialProduct?.type?.let { FurnitureType.from(it) } ?: FurnitureType.CHAIR,
        )
    }
    var typeExpanded by rememberSaveable { mutableStateOf(false) }

    var length by rememberSaveable {
        mutableStateOf(initialProduct?.length?.takeIf { it > 0.0 }?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }.orEmpty())
    }
    var depth by rememberSaveable {
        mutableStateOf(initialProduct?.depth?.takeIf { it > 0.0 }?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }.orEmpty())
    }
    var height by rememberSaveable {
        mutableStateOf(initialProduct?.height?.takeIf { it > 0.0 }?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }.orEmpty())
    }
    var price by rememberSaveable {
        mutableStateOf(initialProduct?.price?.takeIf { it > 0.0 }?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }.orEmpty())
    }

    var colour by rememberSaveable { mutableStateOf(initialProduct?.colour.orEmpty()) }
    var fabric by rememberSaveable { mutableStateOf(initialProduct?.fabric.orEmpty()) }
    var material by rememberSaveable { mutableStateOf(initialProduct?.material.orEmpty()) }
    var equipment by rememberSaveable { mutableStateOf(initialProduct?.equipment.orEmpty()) }
    var remarks by rememberSaveable { mutableStateOf(initialProduct?.remarks.orEmpty()) }

    var existingImageUrl by rememberSaveable { mutableStateOf(initialProduct?.imageUrl) }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var tempCameraFile by remember { mutableStateOf<File?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    var apiErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    val isModelValid = model.trim().isNotBlank()
    val isLengthValid = length.trim().toDoubleOrNull()?.let { it > 0 } ?: false
    val isDepthValid = depth.trim().toDoubleOrNull()?.let { it > 0 } ?: false
    val isHeightValid = height.trim().toDoubleOrNull()?.let { it > 0 } ?: false
    val isPriceValid = price.trim().replace(',', '.').toDoubleOrNull()?.let { it > 0 } ?: false

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success && tempCameraFile != null) {
            val raw = readBytesFromFile(tempCameraFile!!)
            if (raw != null && raw.isNotEmpty()) {
                selectedImageBytes = processImageBytes(raw)
                apiErrorMessage = null
            } else {
                apiErrorMessage = "Nie udało się odczytać zdjęcia z aparatu."
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val raw = readBytesFromUri(context, uri)
            if (raw != null && raw.isNotEmpty()) {
                selectedImageBytes = processImageBytes(raw)
                apiErrorMessage = null
            } else {
                apiErrorMessage = "Nie udało się otworzyć wybranego pliku z galerii."
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted && tempCameraUri != null) {
            cameraLauncher.launch(tempCameraUri!!)
        } else if (!isGranted) {
            apiErrorMessage = "Aplikacja potrzebuje uprawnień do aparatu, aby zrobić zdjęcie."
        }
    }

    val onTakePhotoClick: () -> Unit = {
        val (file, uri) = createTempPictureFileAndUri(context)
        tempCameraFile = file
        tempCameraUri = uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasCamPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (hasCamPermission) {
                cameraLauncher.launch(uri)
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            cameraLauncher.launch(uri)
        }
    }

    val onPickGalleryClick: () -> Unit = {
        galleryLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
    }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = if (isEditing) "Edycja mebla" else "Dodaj nowy mebel",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = if (isEditing) "Zaktualizuj dane produktu i zdjęcie" else "Wypełnij parametry i opcjonalnie dołącz zdjęcie",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            enabled = !isSaving,
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Zamknij")
                        }
                    }

                    HorizontalDivider()

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        AnimatedVisibility(
                            visible = apiErrorMessage != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = apiErrorMessage.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }

                        // Sekcja wyboru zdjęcia
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Zdjęcie produktu",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(Modifier.height(8.dp))

                                val imageToDisplay: Any? = selectedImageBytes ?: resolveImageUrl(existingImageUrl)

                                if (imageToDisplay != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(imageToDisplay)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "Wybrane zdjęcie",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp),
                                            loading = {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                                }
                                            },
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(8.dp),
                                        ) {
                                            Text(
                                                text = if (selectedImageBytes != null) "Nowo wybrane" else "Aktualne zdjęcie",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                selectedImageBytes = null
                                                existingImageUrl = null
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                                    CircleShape,
                                                ),
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Usuń zdjęcie",
                                                tint = MaterialTheme.colorScheme.error,
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(12.dp),
                                            )
                                            .background(MaterialTheme.colorScheme.surfaceContainerLow),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Icon(
                                                Icons.Default.AddAPhoto,
                                                contentDescription = null,
                                                modifier = Modifier.size(36.dp),
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                            )
                                            Text(
                                                text = "Brak zdjęcia (opcjonalne)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    OutlinedButton(
                                        onClick = onTakePhotoClick,
                                        modifier = Modifier.weight(1f),
                                        enabled = !isSaving,
                                    ) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Aparat")
                                    }
                                    OutlinedButton(
                                        onClick = onPickGalleryClick,
                                        modifier = Modifier.weight(1f),
                                        enabled = !isSaving,
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Galeria")
                                    }
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = "Podstawowe informacje",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )

                                OutlinedTextField(
                                    value = model,
                                    onValueChange = { model = it },
                                    label = { Text("Model mebla *") },
                                    placeholder = { Text("np. Narożnik Palermo") },
                                    isError = attemptedSubmit && !isModelValid,
                                    supportingText = if (attemptedSubmit && !isModelValid) {
                                        { Text("Model jest wymagany.") }
                                    } else null,
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )

                                ExposedDropdownMenuBox(
                                    expanded = typeExpanded,
                                    onExpandedChange = { if (!isSaving) typeExpanded = !typeExpanded },
                                ) {
                                    OutlinedTextField(
                                        value = selectedType.displayName,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Typ mebla *") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                                        modifier = Modifier
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                            .fillMaxWidth(),
                                    )
                                    ExposedDropdownMenu(
                                        expanded = typeExpanded,
                                        onDismissRequest = { typeExpanded = false },
                                    ) {
                                        FurnitureType.entries.forEach { type ->
                                            DropdownMenuItem(
                                                text = { Text(type.displayName) },
                                                onClick = {
                                                    selectedType = type
                                                    typeExpanded = false
                                                },
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = price,
                                    onValueChange = { price = it },
                                    label = { Text("Cena (PLN) *") },
                                    placeholder = { Text("np. 1499.00") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    isError = attemptedSubmit && !isPriceValid,
                                    supportingText = if (attemptedSubmit && !isPriceValid) {
                                        { Text("Podaj prawidłową cenę większą od zera.") }
                                    } else null,
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = "Wymiary (cm)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    OutlinedTextField(
                                        value = length,
                                        onValueChange = { length = it },
                                        label = { Text("Długość *") },
                                        placeholder = { Text("cm") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        isError = attemptedSubmit && !isLengthValid,
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                    )
                                    OutlinedTextField(
                                        value = depth,
                                        onValueChange = { depth = it },
                                        label = { Text("Głębokość *") },
                                        placeholder = { Text("cm") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        isError = attemptedSubmit && !isDepthValid,
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                    )
                                    OutlinedTextField(
                                        value = height,
                                        onValueChange = { height = it },
                                        label = { Text("Wysokość *") },
                                        placeholder = { Text("cm") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        isError = attemptedSubmit && !isHeightValid,
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                    )
                                }
                                if (attemptedSubmit && (!isLengthValid || !isDepthValid || !isHeightValid)) {
                                    Text(
                                        text = "Wszystkie wymiary muszą być liczbami większymi od 0.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = "Cechy mebla",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )

                                OutlinedTextField(
                                    value = colour,
                                    onValueChange = { colour = it },
                                    label = { Text("Kolor") },
                                    placeholder = { Text("np. Butelkowa zieleń") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )

                                OutlinedTextField(
                                    value = fabric,
                                    onValueChange = { fabric = it },
                                    label = { Text("Tkanina") },
                                    placeholder = { Text("np. Welur hydrofobowy") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )

                                OutlinedTextField(
                                    value = material,
                                    onValueChange = { material = it },
                                    label = { Text("Materiał konstrukcji") },
                                    placeholder = { Text("np. Drewno sosnowe / płyta meblowa") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                )

                                OutlinedTextField(
                                    value = equipment,
                                    onValueChange = { equipment = it },
                                    label = { Text("Wyposażenie dodatkowe") },
                                    placeholder = { Text("np. Pojemnik na pościel, automat DL") },
                                    modifier = Modifier.fillMaxWidth(),
                                )

                                OutlinedTextField(
                                    value = remarks,
                                    onValueChange = { remarks = it },
                                    label = { Text("Uwagi") },
                                    placeholder = { Text("np. Nóżki chromowane 12cm") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2,
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            enabled = !isSaving,
                        ) {
                            Text("Anuluj")
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = {
                                attemptedSubmit = true
                                if (isModelValid && isLengthValid && isDepthValid && isHeightValid && isPriceValid) {
                                    val parsedPrice = price.trim().replace(',', '.').toDouble()
                                    val parsedLength = length.trim().toDouble()
                                    val parsedDepth = depth.trim().toDouble()
                                    val parsedHeight = height.trim().toDouble()

                                    val request = CreateProductRequest(
                                        model = model.trim(),
                                        type = selectedType.name,
                                        length = parsedLength,
                                        depth = parsedDepth,
                                        height = parsedHeight,
                                        price = parsedPrice,
                                        colour = colour.trim(),
                                        fabric = fabric.trim(),
                                        material = material.trim(),
                                        equipment = equipment.trim(),
                                        remarks = remarks.trim(),
                                        imageUrl = if (selectedImageBytes == null) existingImageUrl else null,
                                    )

                                    val mimeType = "image/jpeg"
                                    val fileName = "mebel_${System.currentTimeMillis()}.jpg"

                                    apiErrorMessage = null
                                    isSaving = true

                                    onSave(
                                        request,
                                        selectedImageBytes,
                                        fileName,
                                        mimeType,
                                        /* onSuccess = */ {
                                            isSaving = false
                                            onDismiss()
                                        },
                                        /* onError = */ { errorMessage ->
                                            isSaving = false
                                            apiErrorMessage = errorMessage
                                        },
                                    )
                                }
                            },
                            enabled = !isSaving,
                        ) {
                            Text(if (isEditing) "Zapisz zmiany" else "Zapisz mebel")
                        }
                    }
                }

                if (isSaving) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {},
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(44.dp),
                                    strokeWidth = 3.5.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = if (selectedImageBytes != null) {
                                        "Wysyłanie zdjęcia i zapisywanie..."
                                    } else {
                                        "Zapisywanie produktu..."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onSave: (
        request: CreateProductRequest,
        imageBytes: ByteArray?,
        fileName: String?,
        mimeType: String?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) -> Unit,
) {
    ProductFormDialog(
        initialProduct = null,
        onDismiss = onDismiss,
        onSave = onSave,
    )
}

private fun createTempPictureFileAndUri(context: Context): Pair<File, Uri> {
    val tempFile = File.createTempFile("camera_", ".jpg", context.cacheDir).apply {
        createNewFile()
    }
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile,
    )
    return Pair(tempFile, uri)
}

private fun readBytesFromUri(context: Context, uri: Uri): ByteArray? {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.readBytes()
        }
    }.onFailure { e ->
        Log.e("AddProductDialog", "Nie udało się odczytać Uri: $uri", e)
    }.getOrNull()
}

private fun readBytesFromFile(file: File): ByteArray? {
    return runCatching {
        if (file.exists() && file.length() > 0) {
            file.readBytes()
        } else null
    }.onFailure { e ->
        Log.e("AddProductDialog", "Nie udało się odczytać pliku: ${file.absolutePath}", e)
    }.getOrNull()
}

private fun processImageBytes(
    rawBytes: ByteArray,
    maxDimension: Int = 1600,
    quality: Int = 82,
): ByteArray {
    return runCatching {
        val orientation = runCatching {
            ExifInterface(ByteArrayInputStream(rawBytes)).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val boundsOnly = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, boundsOnly)

        var sampleSize = 1
        var w = boundsOnly.outWidth
        var h = boundsOnly.outHeight
        if (w > 0 && h > 0) {
            while (w > maxDimension * 1.5 || h > maxDimension * 1.5) {
                sampleSize *= 2
                w /= 2
                h /= 2
            }
        }

        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOpts)
            ?: return rawBytes

        val scale = minOf(1.0, maxDimension.toDouble() / maxOf(decoded.width, decoded.height))
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        if (scale < 1.0) {
            matrix.postScale(scale.toFloat(), scale.toFloat())
        }

        val finalBitmap = if (!matrix.isIdentity) {
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        } else {
            decoded
        }

        val stream = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        val compressed = stream.toByteArray()
        if (compressed.isNotEmpty()) compressed else rawBytes
    }.getOrElse { e ->
        Log.e("AddProductDialog", "Błąd przetwarzania bitmapy, używam surowych bajtów", e)
        rawBytes
    }
}
