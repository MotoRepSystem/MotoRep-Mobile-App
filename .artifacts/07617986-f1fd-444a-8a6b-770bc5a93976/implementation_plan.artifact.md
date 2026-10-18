# Fix Client Activity Crash and API Fetch Issue

## Problem Description
1. **Crash on entering (`se cierra al entrar`)**: The app crashes when navigating to `activity_clientes` (or interacting with `Cliente` entity) because `Cliente.kt` (and other entities) imports `com.android.identity.util.UUID` instead of standard `java.util.UUID`, causing a runtime class loading/resolution failure (`ClassNotFoundException` or `NoClassDefFoundError`).
2. **Not fetching API records (`no me trae los registros de mi api`)**: In `activity_clientes.kt`, `sincronizarPendientes` does not guarantee calling `onFinish()` if an exception occurs, and `Cliente_ApiViewModel` lacks detailed HTTP error logging and fallback handling when fetching remote records fails.

## Proposed Changes

### Data & Models
#### [MODIFY] [Cliente.kt](file:///C:/Users/elkin/OneDrive/Desktop/Proyectos/MotoRep-Mobile-App/app/src/main/java/com/example/motorep_mobile_app/data/entity/Cliente.kt)
- Replace `import com.android.identity.util.UUID` with `import java.util.UUID`.

#### [MODIFY] [Empleado.kt](file:///C:/Users/elkin/OneDrive/Desktop/Proyectos/MotoRep-Mobile-App/app/src/main/java/com/example/motorep_mobile_app/data/entity/Empleado.kt)
- Replace `import com.android.identity.util.UUID` with `import java.util.UUID` for consistency and safety.

#### [MODIFY] [Marca.kt](file:///C:/Users/elkin/OneDrive/Desktop/Proyectos/MotoRep-Mobile-App/app/src/main/java/com/example/motorep_mobile_app/data/entity/Marca.kt)
- Replace `import com.android.identity.util.UUID` with `import java.util.UUID` for consistency and safety.

### ViewModels
#### [MODIFY] [Cliente_ApiViewModel.kt](file:///C:/Users/elkin/OneDrive/Desktop/Proyectos/MotoRep-Mobile-App/app/src/main/java/com/example/motorep_mobile_app/viewmodel/Cliente_ApiViewModel.kt)
- Enhance error handling in `obtenerClientes` and `guardarCliente` to log HTTP error bodies (`HttpException`) similar to `Empleado_ApiViewModel`.

### Activities
#### [MODIFY] [activity_clientes.kt](file:///C:/Users/elkin/OneDrive/Desktop/Proyectos/MotoRep-Mobile-App/app/src/main/java/com/example/motorep_mobile_app/ui/operacional/catalogos/activity_clientes.kt)
- Wrap `sincronizarPendientes` logic with `try / finally` to ensure `onFinish()` is always called even if syncing individual pending items fails.

## Verification Plan

### Automated Tests
- Build the project using `gradle_build("app:assembleDebug")`.

### Manual Verification
- Deploy and open `activity_clientes` on an emulator/device to verify it no longer crashes and correctly syncs/fetches records from the API when online.
