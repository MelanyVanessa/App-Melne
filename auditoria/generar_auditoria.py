"""Llena la plantilla de auditoría (Downloads/auditoria.html) con los resultados de la
auditoría de Melne y guarda una copia en auditoria/auditoria_melne.html.

Uso: python generar_auditoria.py <plantilla.html> <carpeta_evidencias> <salida.html>
Requiere Pillow para reducir las imágenes que se incrustan en el HTML.
"""
import base64
import io
import json
import os
import sys

from PIL import Image

plantilla, carpeta, salida = sys.argv[1], sys.argv[2], sys.argv[3]

ficha = {
    "f-estudiantes": "",
    "f-asignatura": "",
    "f-app": "Melne – Perfumería (versionName 1.0, versionCode 1)",
    "f-package": "com.example.myapp",
    "f-dispositivo": "Honor LGN-LX3 (físico) · 720×1610 px · 320 dpi",
    "f-sdk": "Android 15 (API 35) · minSdk 24 · targetSdk 37 · 20/10/2026",
    "f-repo": "",
}

# Calificación: "C" cumple, "CP" parcial, "NC" no cumple, None = pendiente (requiere usuarios).
items = {
    "01": ("C", "Antes: 4 pestañas en cliente y admin; “Ofertas” y “Pedidos” no llevaban a ningún lado. "
                "Ahora: BottomNavigationView M3 con 3 destinos reales (Inicio, Carrito con insignia de cantidad, "
                "Perfil) y 3 en admin (Panel, Productos, Tienda). Inicio y Panel son raíz; las demás usan "
                "CLEAR_TOP|SINGLE_TOP, así el botón/gesto Atrás vuelve siempre a la raíz. El formulario de "
                "edición no muestra la barra (pantalla de tarea)."),
    "02": ("C", "Márgenes laterales de 20dp y separaciones de 8/12/16/24dp; los valores fuera de la cuadrícula "
                "(43, 239, 241, 6, 10, 14dp) se corrigieron a 40/240/8/12dp. Edge-to-edge con "
                "WindowInsetsCompat: el contenido respeta barra de estado, gestos y teclado; se corrigió un "
                "doble margen bajo la barra inferior consumiendo los insets en la raíz."),
    "03": ("C", "2 familias: Playfair Display (títulos, marca) y Manrope (cuerpo, etiquetas), ambas OFL. "
                "Escala M3 sobrescrita en el tema (HeadlineSmall, TitleMedium, BodyLarge/Medium/Small, "
                "LabelLarge/Medium). Todos los tamaños en sp; mínimo 12sp (antes había 8 textos de 10–11sp)."),
    "04": ("C", "Antes: tema MaterialComponents con modo oscuro roto (morado/ActionBar sobre fondos blancos fijos). "
                "Ahora: Theme.Material3.DayNight con roles semánticos (colorPrimary, surface, onSurface, "
                "onSurfaceVariant, outline, error…) definidos en values/ y values-night/colors.xml; sin colores "
                "fijos en layouts. Claro por defecto y opción Perfil › Apariencia (Claro/Oscuro/Sistema)."),
    "05": ("C", "Ripple en tarjetas, filas y botones (selectableItemBackground / MaterialButton). "
                "Deshabilitado: producto agotado sin botón “+” e imagen atenuada; “Añadir al carrito” "
                "deshabilitado con texto “Agotado”. Cargando: CircularProgressIndicator en listas y "
                "LinearProgressIndicator + texto “Guardando…/Ingresando…” en botones. Error: estado de "
                "error de TextInputLayout. Foco: estados de foco nativos de Material Components."),
    "06": ("C", "Login y formulario de producto migrados a TextInputLayout OutlinedBox (etiqueta flotante "
                "persistente, texto de ayuda “Obligatorio”, placeholder “Ej. …”). inputType: textEmailAddress, "
                "textPassword (con mostrar/ocultar), number para precio/stock/tamaño, textCapWords para nombre/"
                "marca. imeOptions actionNext/actionDone. Selectores como ExposedDropdownMenu. Precio con "
                "prefijo $ y sufijo COP; tamaño con sufijo ml."),
    "07": ("C", "Todos los íconos de interfaz son VectorDrawable 24×24dp (viewport 24) con un solo estilo "
                "(Material relleno) y tinte por rol de color, por lo que funcionan en claro/oscuro. Se "
                "eliminaron íconos sin uso. Las únicas imágenes rasterizadas son fotos (logo, banner, "
                "productos) en WebP/JPEG comprimido. Ícono de la app adaptativo con capa monocroma."),
    "08": (None, "PENDIENTE — requiere pruebas con usuarios reales. Protocolo sugerido: 5 participantes, tarea "
                 "“Busca un perfume de hombre, añádelo al carrito y envía el pedido por WhatsApp”, sin ayuda. "
                 "Registrar éxito/fracaso de cada uno. Meta ≥ 85% (ej. 5/5 = 100%)."),
    "09": (None, "PENDIENTE de medición con usuarios. Medición del flujo principal por el auditor: 3 toques "
                 "desde el catálogo (producto → “Añadir al carrito” → pestaña Carrito → “Realizar pedido por "
                 "WhatsApp”), o 2 usando el “+” de la ficha. El mensaje de WhatsApp se arma solo, sin volver a "
                 "escribir datos. Falta cronometrar el tiempo promedio de los participantes."),
    "10": ("C", "Validación por campo con mensaje que explica cómo corregir (“Escribe el precio en pesos, "
                "solo números (ej. 150000)”, “Escribe un correo válido, por ejemplo nombre@correo.com”). "
                "El error aparece junto al campo, se borra al escribir y los demás datos se conservan. En el "
                "login fallido se conserva el correo y solo se limpia la contraseña. Errores de red con "
                "mensaje específico."),
    "11": ("C", "Medido en el teléfono: arranque en frío 0,89 s (3 mediciones: 897/890/877 ms, am start -W). "
                "Desplazamiento del catálogo: 607 fotogramas, 1,15% lentos, percentil 95 = 14 ms "
                "(dumpsys gfxinfo). Indicadores de progreso en cargas; la compresión de fotos se hace en un "
                "hilo en segundo plano (sin ANR). Respuesta táctil inmediata con ripple."),
    "12": ("C", "Probado con Wi-Fi y datos apagados: el catálogo se muestra desde la caché local "
                "(persistencia de Firebase) con aviso “Sin conexión. Mostrando la información guardada”; si "
                "no hay caché se muestra un estado con botón “Reintentar”. Estados vacíos con ícono, mensaje y "
                "acción: carrito vacío (“Ver catálogo”), sin resultados (“Limpiar filtros”), catálogo vacío "
                "(“Escríbenos por WhatsApp”), admin sin productos (“Agregar producto”). Guardar sin "
                "conexión muestra un aviso en lugar de quedarse esperando."),
    "13": ("C", "MaterialAlertDialog antes de eliminar producto, vaciar carrito, cerrar sesión y descartar "
                "cambios del formulario. Snackbar “Deshacer” al quitar un producto del carrito y al eliminar "
                "un producto del catálogo (se restaura con la misma clave)."),
    "14": (None, "PENDIENTE — aplicar la escala SUS (10 preguntas, Likert 1–5) a los mismos participantes del "
                 "ítem 08 inmediatamente después de la tarea. Puntaje = suma ajustada × 2,5. Meta ≥ 68/100."),
    "15": ("C", "Medido con el árbol de accesibilidad (uiautomator) en 8 pantallas: ningún elemento interactivo "
                "mide menos de 48×48dp (los dos únicos avisos eran elementos cortados por el borde del scroll, "
                "que miden 48dp completos). Antes: botones de cantidad 32dp, editar/eliminar 40dp, “+” 40dp, "
                "“Ver todos”/“Vaciar” ~30dp. Ahora 48dp o más con separación ≥ 8dp; chips con área táctil "
                "mínima de 48dp de Material."),
    "16": ("C", "Contraste calculado con la fórmula WCAG para cada par usado. Antes: “Ver todos” 1,74:1, "
                "texto secundario 3,28:1, stock agotado 2,70:1, borde de campos 1,32:1. Ahora: texto ≥ 5,1:1 "
                "en claro (primario 10,98:1, secundario 5,87:1, enlace 6,29:1) y ≥ 9:1 en oscuro; bordes de "
                "campos 3,76:1. El texto sobre el banner usa un degradado oscuro."),
    "17": ("C", "contentDescription dinámico y específico: “Añadir Miss Dior al carrito”, “Quitar una unidad de "
                "Ari”, “Editar Sauvage”, “Volver”, “Carrito” (con cantidad en la insignia). Fichas con "
                "descripción completa (“Miss Dior, Dior, $160.000 COP”). Imágenes decorativas con "
                "importantForAccessibility=no; títulos marcados como encabezado (accessibilityHeading). "
                "Verificado en el árbol de accesibilidad; se recomienda confirmar con TalkBack activo."),
    "18": ("C", "Probado con font_scale = 2.0 en el teléfono: los textos refluyen dentro de pantallas con "
                "desplazamiento, los nombres pasan a 2 líneas con elipsis y la ficha técnica se reacomoda. "
                "Se corrigió el banner, que cortaba el texto por tener altura fija (ahora crece con el "
                "contenido), y se reemplazaron alturas fijas de campos y botones por minHeight."),
    "19": ("C", "Ningún estado depende solo del color: “Agotado” como insignia de texto sobre la foto; stock "
                "con ícono ✓/✗ + texto (“Stock: 0 • Agotado”); disponibilidad con ícono + texto; chips de "
                "filtro con check visible al seleccionarse; errores con ícono y mensaje. No hay gestos "
                "complejos: todo se hace con toques simples."),
    "20": ("C", "El orden de los elementos en el árbol de accesibilidad sigue el orden visual "
                "(arriba→abajo, izquierda→derecha); se corrigió el encabezado de Inicio, donde “Carrito” "
                "estaba antes que “Buscar”. Las barras fijas (inferior, acciones del detalle) no tapan "
                "contenido: el área de scroll termina sobre ellas. Pendiente: probar con teclado externo."),
}

mejoras = {
    "imp-1-prob": "Ítems 04 y 16 — Modo oscuro roto (tema morado sobre fondos blancos fijos) y contraste "
                  "insuficiente: “Ver todos” 1,74:1, texto secundario 3,28:1, agotado 2,70:1.",
    "imp-1-sol": "Migración a Theme.Material3.DayNight con roles de color en values/ y values-night/colors.xml "
                 "(colorPrimary, colorSurface, colorOnSurfaceVariant, colorOutline…), eliminando colores fijos de "
                 "los layouts. Nueva paleta validada: todo texto ≥ 4,5:1.",
    "imp-2-prob": "Ítems 15 y 18 — Botones de 32–40dp (cantidad, editar, eliminar, “+”) y alturas fijas "
                  "(EditText 48dp, banner 200dp) que recortaban el texto al 200%.",
    "imp-2-sol": "ImageButton/MaterialButton de 48×48dp; minHeight en lugar de layout_height fijo; "
                 "TextInputLayout para campos; banner con ConstraintLayout (imagen 0dp, contenido wrap_content "
                 "con minHeight 200dp).",
    "imp-3-prob": "Ítems 08, 09 y 14 — No hay pruebas con usuarios: falta tasa de éxito, tiempo en la tarea y "
                  "puntaje SUS.",
    "imp-3-sol": "Prueba con 5 usuarios: tarea guiada (buscar perfume → añadir → pedir por WhatsApp), "
                 "cronómetro y conteo de toques, y cuestionario SUS al final. Registrar resultados en los "
                 "ítems 08, 09 y 14.",
}


def a_data_uri(ruta):
    im = Image.open(ruta).convert("RGB")
    im.thumbnail((1100, 1100))
    buf = io.BytesIO()
    im.save(buf, "JPEG", quality=72, optimize=True)
    return "data:image/jpeg;base64," + base64.b64encode(buf.getvalue()).decode("ascii")


evidencias = {}
for num in items:
    ruta = os.path.join(carpeta, f"ev{num}.jpg")
    if os.path.exists(ruta):
        evidencias[num] = {"src": a_data_uri(ruta), "nombre": f"ev{num}.jpg · captura en Honor LGN-LX3"}

datos = {
    "ficha": ficha,
    "items": {k: {"estado": v[0], "obs": v[1]} for k, v in items.items()},
    "mejoras": mejoras,
    "evidencias": evidencias,
}

script = """
  <!-- Resultados de la auditoría de Melne (generado por generar_auditoria.py) -->
  <script>
    const auditoriaMelne = %s;
    document.addEventListener("DOMContentLoaded", function () {
      const d = auditoriaMelne;
      Object.entries(d.ficha).forEach(([id, v]) => { const el = document.getElementById(id); if (el) el.value = v; });
      Object.entries(d.mejoras).forEach(([id, v]) => { const el = document.getElementById(id); if (el) el.value = v; });
      const sufijo = { C: "c", CP: "cp", NC: "nc" };
      Object.entries(d.items).forEach(([num, it]) => {
        const obs = document.getElementById("obs-" + num);
        if (obs) obs.value = it.obs;
        if (it.estado) {
          const radio = document.getElementById("r-" + num + "-" + sufijo[it.estado]);
          if (radio) { radio.checked = true; onRateChange(num, it.estado); }
        }
      });
      Object.entries(d.evidencias).forEach(([num, ev]) => {
        document.getElementById("img-" + num).src = ev.src;
        document.getElementById("cap-" + num).textContent = ev.nombre;
        document.getElementById("prev-" + num).style.display = "flex";
        document.getElementById("hint-" + num).style.display = "none";
        document.getElementById("del-" + num).style.display = "inline-flex";
      });
      recalculateScores();
      // Los cuadros de texto crecen con su contenido (así se ve completo también al exportar a PDF).
      document.querySelectorAll("textarea").forEach((t) => {
        const ajustar = () => { t.style.height = "auto"; t.style.height = (t.scrollHeight + 4) + "px"; };
        ajustar();
        t.addEventListener("input", ajustar);
      });
    });
  </script>
""" % json.dumps(datos, ensure_ascii=False)

html = open(plantilla, encoding="utf-8").read()
marca = "</body>"
assert html.count(marca) == 1, "La plantilla debe tener un único </body>"
html = html.replace(marca, script + marca)
html = html.replace("<title>Auditoría UI/UX & Accesibilidad Android (Material Design 3) - UPC Aguachica</title>",
                    "<title>Auditoría Melne – UI/UX y Accesibilidad</title>")
open(salida, "w", encoding="utf-8").write(html)

total = sum(5 for v in items.values() if v[0] == "C") + sum(2.5 for v in items.values() if v[0] == "CP")
pendientes = [k for k, v in items.items() if v[0] is None]
print(f"Guardado {salida} ({os.path.getsize(salida) // 1024} KB) · puntaje {total}/100 · pendientes: {pendientes}")
