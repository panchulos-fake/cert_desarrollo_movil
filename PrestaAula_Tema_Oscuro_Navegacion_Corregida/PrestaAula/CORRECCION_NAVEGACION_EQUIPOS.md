# Corrección: botón «Equipos» tras confirmar una reserva

## Síntoma
Después de confirmar una reserva y pulsar «Ver mis reservas», el botón inferior «Equipos» podía no abrir la lista de equipos.

## Causa probable
En `ui/navigation/AppRoot.kt`, la barra inferior guardaba/restauraba estados de navegación (`saveState = true`, `restoreState = true`). Esto podía volver a usar un historial anterior justo después de que la confirmación de reserva había retirado la pantalla de reserva del historial con `popUpTo`.

## Cambio aplicado
En los `NavigationBarItem` de `MainScaffold`, la navegación de pestañas utiliza:

```kotlin
nav.navigate(item.route) {
    popUpTo(Routes.EQUIPMENT) { inclusive = false }
    launchSingleTop = true
}
```

De esta forma, al cambiar de pestaña se descartan pantallas intermedias y se evita recuperar estados antiguos. La ruta `EQUIPMENT` se conserva como pantalla raíz. Se retiró el import ya innecesario `findStartDestination`.

**Se mantiene** la corrección anterior de `onDone` de `ReservationScreen`, que retira el formulario ya completado para que no reaparezca el diálogo «Reserva confirmada». Se mantiene también el tema oscuro con azules claros.

## Cómo verificar en Android Studio
1. Iniciar sesión con `docente@marcelapaz.cl` / `Docente123`.
2. Abrir «Equipos» y reservar un recurso disponible.
3. En «Reserva confirmada», pulsar «Ver mis reservas».
4. En la barra inferior, pulsar «Equipos». Debe abrir el inventario.
5. Abrir cualquier otro equipo y entrar a «Reservar equipo». Debe mostrarse un formulario nuevo, sin el diálogo antiguo.
6. Alternar «Mis reservas» ↔ «Equipos» y «Perfil» ↔ «Equipos» varias veces.
7. Comprobar que las reservas guardadas siguen apareciendo en «Mis reservas».

## Limitación de verificación
Aquí se comprobó el cambio de código y la integridad del ZIP, pero **no se realizó compilación Android ni prueba en dispositivo/emulador**. Ejecutar `assembleDebug` y la secuencia anterior en Android Studio.
