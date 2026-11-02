# Tema oscuro con acentos celestes – PrestaAula

Esta versión parte de **PrestaAula_Reserva_Fix.zip** y conserva el arreglo de navegación para reservas.

## Archivos modificados

- `app/src/main/java/com/duoc/prestaaula/ui/theme/Theme.kt`:
  paleta azul noche y celeste de Material 3, fija para la aplicación (no cambia según el tema del teléfono). Se ajustaron colores de texto, botones, tarjetas, campos, avisos e indicadores de estado.
- `app/src/main/res/values/themes.xml`:
  ventana y barras del sistema en tonalidades oscuras también durante el inicio.
- `app/src/main/res/values/colors.xml`:
  fondo del icono y colores para ventana/barras actualizados.

## Colores principales

- Fondo general: `#0B1422`
- Tarjetas y superficies: `#111E2E` a `#2B4056`
- Accento principal (celeste): `#8ED0FF`
- Texto principal: `#E8F3FF`
- Barra superior: `#153B59`

## Verificación en Android Studio

1. Abre `PrestaAula` (carpeta que contiene `settings.gradle.kts`).
2. Ejecuta `Build > Assemble Project` o `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`).
3. Inicia la aplicación y revisa inicio de sesión, inventario, reservas, préstamo, devolución y reportes.
4. Prueba con el teléfono tanto en modo claro como en oscuro: la aplicación debe verse oscura en ambos casos.
5. Verifica la corrección anterior: reserva un equipo, entra a «Mis reservas» y vuelve a reservar otro; no debe reaparecer el mensaje de la reserva anterior.

**Nota:** La compilación Android y la prueba visual requieren Android Studio/SDK; este paquete no afirma haberlas realizado.
