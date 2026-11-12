# FreeTag - Bloqueador Físico NFC (Alternativa Open-Source Profesional a Brick / Monk)

**FreeTag** es una aplicación Android nativa, gratuita y 100% libre de suscripciones que transforma cualquier tarjeta física que ya tengas en casa (tarjeta de crédito/débito contactless, abono de transporte público, carnet universitario, DNIe o llavero NFC) en una **llave de bloqueo física e infranqueable** para tu teléfono móvil.

---

## 🌟 Ecosistema Completo de Características Profesionales

1. **🎴 Vinculación de Tarjetas a Perfiles Específicos:**
   - Asigna perfiles de bloqueo contextuales a cada tarjeta física:
     - **Tarjeta Bancaria / Carnet:** Activa el **Modo Trabajo Profundo** (permite productividad como Slack y llamadas, pero bloquea redes sociales y ocio).
     - **Abono de Transporte:** Activa el **Modo Social Detox** (bloquea estrictamente redes sociales y streaming).
     - **Llavero NFC / Mesita:** Activa el **Modo Sueño** (bloqueo total nocturno; solo teléfono y alarma).
   - Cualquier tarjeta registrada puede desarmar el bloqueo.

2. **🫁 Fricción Consciente "Box Breathing" (12s):**
   - Cuando salta el bloqueo de una app, un botón de respiración guiada de 12 segundos (Inhala 4s • Mantén 4s • Exhala 4s) interrumpe el tic compulsivo del pulgar, permitiendo recuperar el autocontrol antes de tocar la tarjeta física.

3. **⏱️ Pase de Emergencia Temporal de 5 Minutos (Micro-Pass):**
   - Si necesitas consultar urgentemente una entrada, un billete de transporte o un código bancario por SMS, puedes activar un pase de 5 minutos exactos. Al expirar, el bloqueo se reactiva solo sin necesidad de volver a pasar la tarjeta.

4. **📝 PIN Maestro Físico de Papel (Offline Backup PIN):**
   - Para no quedarte nunca bloqueado si te vas de viaje sin tu tarjeta física, puedes configurar un PIN de 6 a 8 dígitos protegido por hash SHA-256 para anotarlo en un papel en tu cartera.

5. **📱 Widget de Pantalla de Inicio para Android:**
   - Widget de escritorio con estética "Digital Monk" (obsidiana y esmeralda) que muestra en tiempo real el estado de presencia, la racha de días y acceso en 1 toque.

6. **🔘 Botón en Ajustes Rápidos (Quick Settings Tile):**
   - Tile *"Modo Presencia"* en la cortina de notificaciones de Android para activar el bloqueo desde cualquier pantalla sin abrir la aplicación.

7. **⏳ Temporizadores de Concentración & Modo Pomodoro:**
   - Sesiones configurables: **Indefinido**, **Pomodoro (25m)**, **45m**, **1 hora** o **2 horas**, con autodesbloqueo triunfal al completarse el tiempo.

8. **📊 Gráfico Semanal de Progreso (Weekly Focus Analytics):**
   - Gráfico de barras interactivo con los minutos de concentración alcanzados los últimos 7 días.

9. **🛡️ Escudo Anti-Desinstalación (Anti-Bypass Protection):**
   - Detección e intercepción en tiempo real de cualquier intento de abrir el desinstalador de paquetes mientras el bloqueo esté activo.

10. **🔔 Notificación Persistente Anti-Cierre (Foreground Presence):**
    - Protege al servicio frente al cierre por optimizadores de batería agresivos de capas como Xiaomi (HyperOS) o Samsung (OneUI).

11. **🛑 Modo Estricto Absoluto:**
    - Deshabilita el desbloqueo de emergencia de 60s para usuarios que buscan un compromiso físico 100% inquebrantable.

12. **🏰 Modo Búnker (Kiosko Exclusivo / Whitelist Inversa):**
    - Da la vuelta a la lógica: en vez de bloquear apps una a una, bloquea **todo el teléfono** excepto tus apps de máxima prioridad (Teléfono, Mapas, etc.). Ideal para oposiciones o estudio extremo.

13. **⏳ Pausa Consciente "One-Sec Friction" (0s a 15s):**
    - Retardo configurable al abrir una app bloqueada que exige esperar 5, 10 o 15 segundos antes de permitir pases temporales, enfriando por completo el impulso dopaminérgico.

14. **⏳ Calculadora de Vida Recuperada (Reclaimed Life Analytics):**
    - Convierte automáticamente cada tentación compulsiva interceptada en tiempo real de vida ahorrado (~15 minutos de scroll infinito por impulso evitado).

15. **💾 Copia de Seguridad Offline del Llavero (Backup & Restore JSON):**
    - Exporta e importa tus tarjetas, configuraciones y listas de bloqueo en un formato JSON seguro 100% offline, para migrar de móvil sin depender de servidores ni nubes.

16. **📳 Háptica Mecánica de Caja Fuerte ("Safe Lock Heavy Haptics"):**
    - Patrón táctil inmersivo que simula el mecanismo y pestillo de una caja fuerte pesada al bloquear el teléfono con tu tarjeta física.

---

## 🔒 Privacidad y Seguridad (Zero-Knowledge)

FreeTag opera bajo el principio de **conocimiento cero**:
1. **Sin Permisos de Internet:** No contiene `<uses-permission android:name="android.permission.INTERNET" />`. Tus datos nunca salen de tu teléfono.
2. **Sin Lectura de Datos Financieros:** Utiliza `enableReaderMode` para leer **estrictamente** el UID del chip (`tag.id`). No lee cuentas, saldos ni numeración bancaria.
3. **Hashing Irreversible SHA-256:** El UID se convierte en un hash SHA-256 al instante.

---

## 🚀 Instalación Rápida (Sin necesidad de Android Studio)

El ejecutable compilado está listo en la raíz del proyecto como [**`FreeTag.apk`**](FreeTag.apk).

### Opción A: Instalador Automático por Cable USB (1 Clic)
1. Conecta tu teléfono Android por USB al PC con la **Depuración por USB** activada.
2. Haz doble clic en [**`instalar_en_movil.bat`**](instalar_en_movil.bat).

### Opción B: Descarga Directa por WiFi
1. Haz doble clic en [**`descargar_en_movil.bat`**](descargar_en_movil.bat).
2. Entra a la dirección mostrada desde el navegador de tu móvil para descargar el APK.

---

## 🎴 Plantilla Imprimible de Tarjeta Física (Tamaño Real CR80)

Si quieres enseñar la tarjeta o fabricar tu propia llave física personalizada:
1. Haz doble clic en [**`imprimir_tarjeta.bat`**](imprimir_tarjeta.bat) (o abre [`tarjeta_freetag_imprimible.html`](tarjeta_freetag_imprimible.html) en tu navegador).
2. Pulsa **Ctrl + P** para imprimir en papel o cartulina (selecciona **Escala: 100% / Tamaño real**).
3. Recorta la cara delantera y trasera con unas tijeras o cúter y pégalas sobre cualquier tarjeta vieja o insértale una pegatina NFC adhesiva.
