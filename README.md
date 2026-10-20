# Melne — Perfumería (Android)

App Android de catálogo y pedidos para **Melne**, perfumería de Aguachica, Cesar. Proyecto de clase del Programa de Ingeniería de Sistemas, Universidad Popular del Cesar, seccional Aguachica.

## Funciones

**Cliente**
- Catálogo en cuadrícula con filtros (Hombre, Mujer, Unisex, Nuevos) y búsqueda.
- Detalle de producto con disponibilidad y ficha técnica.
- Carrito con cantidades, opción de deshacer y pedido por WhatsApp con el mensaje ya armado.
- Perfil con contacto, Instagram, ubicación y apariencia (claro, oscuro o según el sistema).
- Funciona sin conexión con la última copia del catálogo.

**Administrador**
- Inicio de sesión con Firebase Auth. Solo pueden entrar las cuentas que estén en `/admins`.
- Panel con datos reales: total de productos, con stock, agotados, valor del inventario y stock bajo.
- Crear, editar y eliminar productos (con opción de deshacer). Las fotos se comprimen en el teléfono antes de guardarse.

## Tecnología

- Java, Android SDK 37 (minSdk 24), Material Design 3 con tema claro/oscuro.
- Firebase Realtime Database (con persistencia local) y Firebase Authentication.
- Glide para imágenes, RecyclerView y ConstraintLayout.
- Tipografías Playfair Display y Manrope (SIL Open Font License).

## Cómo ejecutarla

1. Abre el proyecto en Android Studio y ejecuta la configuración `app`.
2. La app usa el proyecto de Firebase `my-app-22977`, definido en `app/google-services.json`. Para usar tu propio proyecto de Firebase:
   1. Crea el proyecto y registra la app Android con el paquete `com.example.myapp`.
   2. Reemplaza `app/google-services.json` por el que descargues de tu proyecto.
   3. Activa **Authentication → Correo/contraseña** y crea la cuenta de administrador.
   4. En **Realtime Database**, agrega el nodo `admins/<UID de la cuenta>: true`.
   5. Publica las reglas de [`database.rules.json`](database.rules.json).
3. Pruebas unitarias: `./gradlew testDebugUnitTest`.

La versión de prueba (debug) incluye en el Panel un botón **"Cargar catálogo de ejemplo"** que reemplaza el catálogo por 20 referencias de ejemplo. Ese botón no existe en la versión de lanzamiento (release).

## Estructura

```
app/src/main/java/com/example/myapp/
  SplashActivity, HomeActivity, ProductDetailActivity, CartActivity, ProfileActivity   (cliente)
  LoginActivity, AdminDashboardActivity, AdminProductsActivity, ProductFormActivity    (admin)
  Producto, Productos, Carrito, Formato, Imagenes, Navegacion, Conexion, Tema           (lógica)
app/src/debug/assets/catalogo_ejemplo.json   catálogo de ejemplo (solo debug)
auditoria/                                   auditoría UI/UX y accesibilidad con evidencias
database.rules.json                          reglas de seguridad de Firebase
```

## Auditoría UI/UX y accesibilidad

La carpeta [`auditoria/`](auditoria/) contiene la auditoría con la plantilla Material 3 / WCAG 2.2 AA: [`auditoria_melne.html`](auditoria/auditoria_melne.html), su versión en PDF y las capturas tomadas en un dispositivo real.

## Créditos

- Fotos de productos y del banner: [Pexels](https://www.pexels.com/license/), con licencia de uso gratuito. El enlace de cada foto está en `catalogo_ejemplo.json`.
- Proyecto académico sin fines comerciales. Las marcas de perfumes mencionadas pertenecen a sus dueños.
