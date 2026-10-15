# Plan de Implementación: Obtener y Mostrar Clientes en RecyclerView

Resolver los errores en `activity_clientes.kt` para realizar correctamente la petición GET a la API y mostrar la lista de clientes en el `RecyclerView`.

## User Review Required

> [!IMPORTANT]
> Se identificaron tres problemas principales que impiden que los clientes se muestren correctamente:
> 1. **Declaración errónea del Adapter:** En `activity_clientes.kt`, la propiedad a nivel de clase `adapter` está declarada como `Cliente_ApiViewModel` en lugar de `Cliente_Adapter`.
> 2. **Función `toEntity()` no existente:** Se intenta llamar a `it.toEntity()` sobre `Cliente_dto`, pero dicha función de extensión no está definida en el proyecto.
> 3. **Conflicto entre fuentes de datos (Room vs API):** Actualmente se observa el LiveData local de Room y también se llama directamente al ViewModel de la API, lo cual puede generar comportamientos inesperados o duplicados.

## Proposed Changes

### Módulo de Clientes (UI & Data)

#### [MODIFY] [activity_clientes.kt](file:///C:/Users/elkin/OneDrive/Desktop/Proyectos/MotoRep-Mobile-App/app/src/main/java/com/example/motorep_mobile_app/ui/operacional/catalogos/activity_clientes.kt)
- Corregir la declaración de la variable miembro `adapter` a `Cliente_Adapter`.
- Definir correctamente la extensión `toEntity()` o mapear los campos de `Cliente_dto` a la entidad `Cliente` al recibir la respuesta de la API.
- Unificar la carga de datos mediante `apiViewModel.obtenerClientes` o el ViewModel local según la arquitectura elegida.

## Verification Plan

### Automated Tests
- Compilación del proyecto (`gradle_build`) para verificar que no existan errores de tipos ni referencias no resueltas.

### Manual Verification
- Ejecutar la aplicación en el dispositivo/emulador, iniciar sesión y navegar a la pantalla de Clientes (`activity_clientes`) para verificar que la lista se cargue y dibuje correctamente en el `RecyclerView`.
