# Plan de Implementación: Sincronización en `activity_agregarcliente`

Ajustar la lógica de creación y sincronización de clientes en `activity_agregarcliente.kt` y `Cliente_dto.kt` para garantizar que funcione correctamente tanto online como offline.

## User Review Required

> [!IMPORTANT]
> Se detectaron tres detalles clave en la implementación actual de `activity_agregarcliente.kt` y `Cliente_dto.kt`:
> 1. **Falta inicializar `apiRepository`:** Se intenta llamar a `apiRepository.guardarCliente(...)` pero no está instanciado en la Activity.
> 2. **Incompatibilidad en `Cliente_dto` al crear:** Se pasa `id = null` (cuando el DTO espera un `Int`) y `sincronizado` (que no existe en el DTO). Se debe usar el mapper inverso `cliente.toDto()` o ajustar los parámetros.
> 3. **Manejo de ID del servidor:** Al crear un nuevo cliente en la API, el ID que devuelve el servidor debe actualizarse en Room junto con `sincronizado = true`.

## Proposed Changes

### Módulo UI & DTO

#### [MODIFY] [Cliente_dto.kt](file:///C:/Users/elkin/OneDrive/Desktop/Proyectos/MotoRep-Mobile-App/app/src/main/java/com/example/motorep_mobile_app/data/remote/dto/Cliente_dto.kt)
- Opcionalmente permitir `id: Int? = null` en `Cliente_dto` para cuando se crea un registro nuevo que aún no tiene ID en el servidor.

#### [MODIFY] [activity_agregarcliente.kt](file:///C:/Users/elkin/OneDrive/Desktop/Proyectos/MotoRep-Mobile-App/app/src/main/java/com/example/motorep_mobile_app/activity_agregarcliente.kt)
- Instanciar `apiRepository` usando `Cliente_ApiRepository(RetrofitClient.create(this))`.
- Utilizar el mapper `cliente.toDto()` o construir el DTO correctamente para enviarlo a la API.
- Manejar la respuesta del servidor para actualizar el `serverId` local y marcar el registro como sincronizado.

## Verification Plan

### Automated Tests
- Compilación del proyecto (`gradle_build`).

### Manual Verification
- Probar agregar un cliente con conexión a internet y verificar que se guarde en Room y se sincronice con el API.
- Probar agregar un cliente sin conexión y verificar que se guarde localmente con `sincronizado = false`.
