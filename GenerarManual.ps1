
$ErrorActionPreference = "Stop"
Get-Process -Name "WINWORD" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 3

$docPath = "C:\Users\Juan Loaiza\Desktop\Proyecto Gestion Residuos\GestionResiduosBack\Manual_API_GestionResiduos.docx"
if (Test-Path $docPath) { Remove-Item $docPath -Force }

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$doc = $word.Documents.Add()
$sel = $word.Selection

function H1($t){ $sel.Font.Size=16;$sel.Font.Bold=$true;$sel.Font.Color=0;$sel.Font.Italic=$false;$sel.Font.Name="Calibri";$sel.TypeText($t);$sel.TypeParagraph() }
function H2($t){ $sel.Font.Size=12;$sel.Font.Bold=$true;$sel.Font.Color=0;$sel.Font.Italic=$false;$sel.Font.Name="Calibri";$sel.TypeText($t);$sel.TypeParagraph() }
function P($t){  $sel.Font.Size=11;$sel.Font.Bold=$false;$sel.Font.Color=0;$sel.Font.Italic=$false;$sel.Font.Name="Calibri";$sel.TypeText($t);$sel.TypeParagraph() }
function PB($t){ $sel.Font.Size=11;$sel.Font.Bold=$true;$sel.Font.Color=0;$sel.Font.Italic=$false;$sel.Font.Name="Calibri";$sel.TypeText($t);$sel.Font.Bold=$false;$sel.TypeParagraph() }
function C($t){  $sel.Font.Size=9;$sel.Font.Bold=$false;$sel.Font.Color=0;$sel.Font.Italic=$false;$sel.Font.Name="Courier New";$sel.TypeText($t);$sel.TypeParagraph();$sel.Font.Name="Calibri" }
function BR(){ $sel.Font.Name="Calibri";$sel.TypeParagraph() }
function PB2(){ $sel.InsertBreak([Microsoft.Office.Interop.Word.WdBreakType]::wdPageBreak) }

# PORTADA
$sel.ParagraphFormat.Alignment=1;$sel.Font.Size=24;$sel.Font.Bold=$true;$sel.Font.Color=0;$sel.Font.Name="Calibri"
$sel.TypeText("Manual Tecnico de API");$sel.TypeParagraph()
$sel.TypeText("Sistema de Gestion de Residuos");$sel.TypeParagraph()
$sel.Font.Size=12;$sel.Font.Bold=$false;$sel.TypeText("Version 1.0 - Mayo 2026");$sel.TypeParagraph()
$sel.ParagraphFormat.Alignment=0
PB2

# 1. INTRODUCCION
H1 "1. INTRODUCCION"
P "Este documento describe todos los endpoints REST del Sistema de Gestion de Residuos (Spring Boot 3.x)."
P "Incluye metodo HTTP, URL, autorizacion requerida, body de request y ejemplos de response para cada endpoint."
P "Base URL: http://localhost:8080"
P "Autenticacion: Authorization: Bearer <JWT_TOKEN>"
P "Roles disponibles: CIUDADANO, ADMINISTRADOR"
BR

# 2. AUTENTICACION
H1 "2. MODULO DE AUTENTICACION (/auth)"
H2 "POST /auth/register/user - Registrar usuario ciudadano"
PB "Acceso: Publico"
PB "Request Body (application/json):"
C '{"documentNumber":123456,"names":"Juan","lastName":"Perez","email":"juan@correo.com","birthDate":"2000-01-15","neighborhoodId":1,"address":"Calle 1 # 2-3","password":"pass1234","phoneNumber":"3001234567"}'
PB "Response 200:"
C '{"message":"Registro exitoso. Verifica tu correo.","email":"juan@correo.com","status":null}'
PB "Response 400 - Datos invalidos:"
C '{"status":400,"error":"Bad Request","message":"El email debe tener un formato valido","path":"/auth/register/user"}'
BR
H2 "POST /auth/login - Iniciar sesion"
PB "Acceso: Publico"
PB "Request Body:"
C '{"email":"juan@correo.com","password":"pass1234"}'
PB "Response 200:"
C '{"accessToken":"eyJhbGciOiJIUzI1NiJ9.xxx","refreshToken":"eyJhbGciOiJIUzI1NiJ9.yyy","email":"juan@correo.com","nickName":"juan123","photo":"/uploads/profile/foto.jpg","role":"CIUDADANO"}'
PB "Response 401 - Credenciales invalidas:"
C '{"status":401,"error":"Unauthorized","message":"Credenciales invalidas","path":"/auth/login"}'
BR
H2 "GET /auth/verify?token={token} - Verificar cuenta"
PB "Acceso: Publico | Query: token (string)"
PB "Response 200:"
C '{"accessToken":"eyJ...","refreshToken":"eyJ...","email":"juan@correo.com","role":"CIUDADANO"}'
PB "Response 400 - Token invalido:"
C '{"status":400,"error":"Bad Request","message":"Token invalido o expirado"}'
BR
H2 "POST /auth/refresh - Renovar access token"
PB "Acceso: Publico | Body: string (solo el refreshToken)"
C '"eyJhbGciOiJIUzI1NiJ9.yyy"'
PB "Response 200:"
C '{"accessToken":"eyJ_nuevo...","refreshToken":"eyJ_nuevo..."}'
BR
H2 "POST /auth/forgot-password - Recuperacion de contrasena"
PB "Acceso: Publico | Body: {email}"
C '{"email":"juan@correo.com"}'
PB "Response 200:"
C '{"message":"Se envio un correo de recuperacion","email":"juan@correo.com","status":null}'
BR
H2 "POST /auth/reset-password - Confirmar nueva contrasena"
PB "Acceso: Publico | Body: {token, newPassword}"
C '{"token":"a1b2c3d4-uuid","newPassword":"nuevaPass123"}'
PB "Response 200:"
C '{"message":"Contrasena actualizada correctamente","email":"juan@correo.com","status":null}'
BR
H2 "POST /auth/login/google - Login con Google"
PB "Acceso: Publico"
C '{"email":"juan@gmail.com","googleId":"108394827364..."}'
PB "Response 200:"
C '{"accessToken":"eyJ...","refreshToken":"eyJ...","email":"juan@gmail.com","nickName":"juan","role":"CIUDADANO"}'
BR
H2 "POST /auth/resend-verification - Reenviar verificacion"
PB "Acceso: Publico | Body: {email}"
C '{"email":"juan@correo.com"}'
PB "Response 200:"
C '{"message":"Correo de verificacion reenviado correctamente","email":"juan@correo.com","status":null}'
BR

# 3. USUARIOS
PB2
H1 "3. MODULO DE USUARIOS (/api/users)"
H2 "GET /api/users/profile - Obtener perfil"
PB "Acceso: JWT requerido | Sin body"
PB "Response 200:"
C '{"documentNumber":123456,"names":"Juan","lastName":"Perez","nickName":"juan123","documentType":"CC","email":"juan@correo.com","birthDate":"2000-01-15","neighborhoodName":"Chapinero","neighborhoodId":1,"address":"Calle 1 # 2-3","photo":"/uploads/profile_photo/123456.jpg","phoneNumber":"3001234567","status":"VERIFICADO","createdAt":"2026-01-01","roleName":"CIUDADANO","canUpdate":true,"nextUpdateAvailable":null,"points":150}'
PB "Response 401:"
C '{"status":401,"error":"Unauthorized","message":"No estas autenticado. Debes enviar un token JWT valido."}'
BR
H2 "PUT /api/users/profile - Actualizar perfil"
PB "Acceso: JWT requerido | Content-Type: multipart/form-data"
PB "Form fields:"
P '  names (string, opcional), lastName (string, opcional), email (string, opcional)'
P '  address (string, opcional), neighborhoodId (int, opcional), photo (file, opcional)'
PB "Response 200:"
C '{"documentNumber":123456,"names":"Juan Actualizado","lastName":"Perez","email":"nuevo@correo.com","address":"Carrera 5 # 10-20","photo":"/uploads/profile_photo/123456.jpg","canUpdate":false,"nextUpdateAvailable":"2026-06-11"}'
PB "Response 400 - Ya actualizo este mes:"
C '{"status":400,"error":"Bad Request","message":"Solo puedes actualizar tu perfil una vez por mes. Proxima actualizacion: 2026-06-11"}'
BR
H2 "GET /api/users/admin/list - Listar usuarios (admin)"
PB "Acceso: JWT + ADMINISTRADOR | Sin body"
PB "Response 200:"
C '[{"documentNumber":123456,"names":"Juan","lastName":"Perez","photo":"/uploads/profile_photo/123456.jpg","status":"VERIFICADO","email":"juan@correo.com"},{"documentNumber":654321,"names":"Maria","lastName":"Lopez","photo":null,"status":"ACTIVO","email":"maria@correo.com"}]'
PB "Response 403:"
C '{"status":403,"error":"Forbidden","message":"No tienes permisos para acceder a este recurso."}'
BR
H2 "GET /api/users/admin/{documentNumber} - Detalle usuario (admin)"
PB "Acceso: JWT + ADMINISTRADOR | Path: documentNumber (int)"
PB "Response 200:"
C '{"documentNumber":123456,"names":"Juan","lastName":"Perez","nickName":"juan123","email":"juan@correo.com","birthDate":"2000-01-15","neighborhoodName":"Chapinero","address":"Calle 1","photo":null,"phoneNumber":"3001234567","status":"VERIFICADO","createdAt":"2026-01-01","roleName":"CIUDADANO","points":150}'
PB "Response 404:"
C '{"status":404,"error":"Not Found","message":"Usuario no encontrado con documento: 999"}'
BR
H2 "PATCH /api/users/admin/{documentNumber}/status - Cambiar estado usuario"
PB "Acceso: JWT + ADMINISTRADOR | Path: documentNumber"
PB "Request Body:"
C '{"status":"INACTIVO"}'
PB "Response 200:"
C '{"documentNumber":123456,"names":"Juan","status":"INACTIVO","email":"juan@correo.com"}'
BR

# 4. ECOPUNTOS
PB2
H1 "4. MODULO DE ECOPUNTOS (/api/ecopoints)"
H2 "GET /api/ecopoints - Listar todos"
PB "Acceso: Publico | Sin body"
PB "Response 200:"
C '[{"id":1,"name":"EcoPunto Norte","address":"Calle 10 # 5-20","latitude":4.6097,"longitude":-74.0817,"description":"Residuos reciclables","status":"ACTIVO","residueTypes":["PLASTICO","PAPEL"],"neighborhoodId":1,"openingTime":"08:00","closingTime":"18:00"}]'
BR
H2 "GET /api/ecopoints/active - Listar activos"
PB "Acceso: Publico | Sin body | Response: igual al anterior pero solo ACTIVO"
BR
H2 "GET /api/ecopoints/{id} - Obtener por ID"
PB "Acceso: Publico | Path: id (Long)"
PB "Response 200:"
C '{"id":1,"name":"EcoPunto Norte","address":"Calle 10 # 5-20","latitude":4.6097,"longitude":-74.0817,"description":"Residuos reciclables","status":"ACTIVO","residueTypes":["PLASTICO","PAPEL"],"openingTime":"08:00","closingTime":"18:00"}'
PB "Response 404:"
C '{"status":404,"error":"Not Found","message":"EcoPunto no encontrado con id: 99"}'
BR
H2 "GET /api/ecopoints/neighborhood/{neighborhoodId} - Por barrio"
PB "Acceso: Publico | Path: neighborhoodId (int)"
PB "Response 200: Array de EcoPuntos del barrio indicado"
BR
H2 "GET /api/ecopoints/residue-type/{residueType} - Por tipo de residuo"
PB "Acceso: Publico | Path: residueType (ej: PLASTICO, PAPEL, ORGANICO)"
PB "Response 200: Array de EcoPuntos que aceptan ese tipo de residuo"
BR
H2 "POST /api/ecopoints - Crear ecopunto"
PB "Acceso: JWT + ADMINISTRADOR"
PB "Request Body:"
C '{"name":"EcoPunto Sur","address":"Carrera 5 # 20-10","latitude":4.5878,"longitude":-74.1125,"description":"Residuos electronicos","residueTypes":["ELECTRONICO","PELIGROSO"],"neighborhoodId":2,"openingTime":"09:00","closingTime":"17:00"}'
PB "Response 201:"
C '{"id":10,"name":"EcoPunto Sur","address":"Carrera 5 # 20-10","latitude":4.5878,"longitude":-74.1125,"status":"ACTIVO","residueTypes":["ELECTRONICO","PELIGROSO"],"openingTime":"09:00","closingTime":"17:00"}'
PB "Response 400 - Coordenadas invalidas:"
C '{"status":400,"error":"Bad Request","message":"La latitud debe estar entre -90 y 90"}'
BR
H2 "PUT /api/ecopoints/{id} - Actualizar ecopunto"
PB "Acceso: JWT + ADMINISTRADOR | Body: mismos campos del POST"
PB "Response 200: EcoPunto actualizado | Response 404: No encontrado"
BR
H2 "PATCH /api/ecopoints/{id}/status - Cambiar estado"
PB "Acceso: JWT + ADMINISTRADOR | Query: status (ACTIVO / INACTIVO)"
PB "Response 200:"
C '{"id":1,"name":"EcoPunto Norte","status":"INACTIVO"}'
BR
H2 "DELETE /api/ecopoints/{id} - Eliminar"
PB "Acceso: JWT + ADMINISTRADOR"
PB "Response 204 No Content (eliminado exitosamente)"
PB "Response 404: EcoPunto no encontrado"
BR

# 5. REPORTES
PB2
H1 "5. MODULO DE REPORTES (/api/reports)"
H2 "POST /api/reports - Crear reporte"
PB "Acceso: JWT requerido | Content-Type: multipart/form-data"
PB "Tipos: punto_critico (requiere latitude, longitude, image), incumplimiento_calendario (requiere calendarId)"
PB "Form fields - Punto Critico:"
P "  type=punto_critico, categoryId=1, description=Basura acumulada en la esquina, latitude=4.6097, longitude=-74.0817, image=(archivo)"
PB "Form fields - Incumplimiento Calendario:"
P "  type=incumplimiento_calendario, categoryId=2, description=No pasaron a recoger, calendarId=5"
PB "Response 201:"
C '{"id":1,"type":"punto_critico","categoryId":1,"categoryName":"Basura","description":"Basura acumulada","latitude":4.6097,"longitude":-74.0817,"imageUrl":"/uploads/reportes/img1.jpg","status":"pendiente","userId":123456,"userName":"Juan Perez","createdAt":"2026-05-11","newAchievements":[]}'
PB "Response 400 - Faltan campos obligatorios:"
C '{"status":400,"error":"Bad Request","message":"La latitud es obligatoria (HU10)"}'
BR
H2 "GET /api/reports - Listar todos los reportes"
PB "Acceso: JWT requerido | Sin body"
PB "Response 200: Array de ReportDTO"
C '[{"id":1,"type":"punto_critico","categoryName":"Basura","description":"Basura en la via","status":"pendiente","userName":"Juan","createdAt":"2026-05-11"},{"id":2,"type":"incumplimiento_calendario","categoryName":"Calendario","description":"No pasaron","status":"resuelto","createdAt":"2026-05-10"}]'
BR
H2 "GET /api/reports/my-reports - Mis reportes"
PB "Acceso: JWT requerido | Sin body"
PB "Response 200: Array de mis reportes | Response 200 lista vacia si no tiene reportes:"
C '[]'
BR
H2 "GET /api/reports/{id} - Obtener reporte por ID"
PB "Acceso: JWT requerido"
PB "Response 200: ReportDTO completo | Response 404: No encontrado"
BR
H2 "GET /api/reports/type/{type} - Filtrar por tipo"
PB "Acceso: JWT requerido | Path: type (punto_critico | incumplimiento_calendario)"
PB "Response 200: Array de reportes del tipo indicado"
BR
H2 "GET /api/reports/status/{status} - Filtrar por estado"
PB "Acceso: JWT requerido | Path: status (pendiente | en_proceso | resuelto)"
PB "Response 200: Array de reportes con ese estado"
BR
H2 "PATCH /api/reports/{id}/status - Cambiar estado"
PB "Acceso: JWT requerido | Query: newStatus (pendiente | en_proceso | resuelto)"
PB "Response 200:"
C '{"id":1,"status":"resuelto","resolvedAt":"2026-05-11"}'
BR
H2 "GET /api/reports/search - Busqueda paginada con filtros"
PB "Acceso: JWT requerido"
PB "Query params: status, type, dateFrom (YYYY-MM-DD), dateTo, categoryId, page (default 0), size (default 10)"
PB "Ejemplo: /api/reports/search?status=pendiente&type=punto_critico&page=0&size=10"
PB "Response 200:"
C '{"content":[{"id":1,"type":"punto_critico","status":"pendiente","description":"Basura"}],"totalElements":1,"totalPages":1,"number":0,"size":10}'
BR
H2 "GET /api/reports/statistics - Estadisticas generales"
PB "Acceso: JWT requerido | Sin body"
PB "Response 200:"
C '{"totalReports":120,"pendingReports":45,"resolvedReports":70,"inProgressReports":5,"byType":{"punto_critico":80,"incumplimiento_calendario":40}}'
BR
H2 "GET /api/reports/stats - Estadisticas filtradas (ADMIN)"
PB "Acceso: JWT + ADMINISTRADOR"
PB "Query: startDate (requerido), endDate (requerido), status (opcional)"
PB "Ejemplo: /api/reports/stats?startDate=2026-01-01&endDate=2026-05-31"
PB "Response 200:"
C '{"total":50,"byStatus":[{"status":"pendiente","count":20},{"status":"resuelto","count":30}],"trend":[{"date":"2026-05-01","count":5},{"date":"2026-05-02","count":8}],"resolvedPercentage":60.0,"pendingPercentage":40.0,"avgResolutionTime":3.5}'
BR

# 6. CATEGORIAS REPORTES
PB2
H1 "6. MODULO DE CATEGORIAS DE REPORTES (/api/report-categories)"
H2 "GET /api/report-categories - Listar todas"
PB "Acceso: Publico | Sin body"
PB "Response 200:"
C '[{"id":1,"name":"Basura","description":"Acumulacion de residuos","status":"ACTIVO"},{"id":2,"name":"Calendario","description":"Incumplimiento recoleccion","status":"ACTIVO"}]'
BR
H2 "GET /api/report-categories/active - Solo activas"
PB "Acceso: Publico | Response igual a anterior pero filtrado por status=ACTIVO"
BR
H2 "GET /api/report-categories/{id} - Por ID"
PB "Acceso: Publico | Response 200: ReportCategoryDTO | Response 404: No encontrada"
BR
H2 "POST /api/report-categories - Crear categoria"
PB "Acceso: JWT + ADMINISTRADOR"
PB "Request Body:"
C '{"name":"Contaminacion","description":"Contaminacion de fuentes de agua"}'
PB "Response 201:"
C '{"id":5,"name":"Contaminacion","description":"Contaminacion de fuentes de agua","status":"ACTIVO"}'
PB "Response 400 - Nombre vacio:"
C '{"status":400,"error":"Bad Request","message":"El nombre de la categoria es obligatorio"}'
BR
H2 "PUT /api/report-categories/{id} - Actualizar"
PB "Acceso: JWT + ADMINISTRADOR | Body: {name, description, status}"
PB "Response 200: Categoria actualizada"
BR
H2 "PATCH /api/report-categories/{id}/status - Cambiar estado"
PB "Acceso: JWT + ADMINISTRADOR | Query: status (ACTIVO | INACTIVO)"
PB "Response 200:"
C '{"id":1,"name":"Basura","description":"Acumulacion de residuos","status":"INACTIVO"}'
BR
H2 "DELETE /api/report-categories/{id} - Eliminar"
PB "Acceso: JWT + ADMINISTRADOR | Response 204 No Content"
BR

# 7. CALENDARIO
PB2
H1 "7. MODULO DE CALENDARIO DE RECOLECCION (/api/schedules)"
H2 "GET /api/schedules/district/{districtId} - Por localidad"
PB "Acceso: Publico | Path: districtId (int)"
PB "Response 200:"
C '[{"id":1,"districtId":3,"districtName":"Chapinero","residueType":"ORGANICO","dayOfWeek":"LUNES","startTime":"08:00","endTime":"12:00","description":"Recoleccion organicos lunes","status":"ACTIVO"}]'
BR
H2 "GET /api/schedules/district/{districtId}/day/{dayOfWeek} - Por localidad y dia"
PB "Acceso: Publico | Path: districtId, dayOfWeek (LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,DOMINGO)"
PB "Response 200: Array de CollectionScheduleDTO"
BR
H2 "GET /api/schedules/district/{districtId}/residue-type/{residueType} - Por localidad y tipo"
PB "Acceso: Publico | Path: districtId, residueType"
PB "Response 200: Array de CollectionScheduleDTO"
BR
H2 "GET /api/schedules/{id} - Detalle de horario"
PB "Acceso: Publico | Response 200: CollectionScheduleDTO | Response 404: No encontrado"
BR
H2 "GET /api/schedules - Todos los horarios (admin)"
PB "Acceso: JWT + ADMINISTRADOR | Response 200: Array completo"
BR
H2 "POST /api/schedules - Crear horario"
PB "Acceso: JWT + ADMINISTRADOR"
PB "Request Body:"
C '{"districtId":3,"residueType":"ORGANICO","dayOfWeek":"LUNES","startTime":"08:00","endTime":"12:00","description":"Recoleccion organicos"}'
PB "Response 201:"
C '{"id":10,"districtId":3,"districtName":"Chapinero","residueType":"ORGANICO","dayOfWeek":"LUNES","startTime":"08:00","endTime":"12:00","status":"ACTIVO"}'
BR
H2 "PUT /api/schedules/{id} - Actualizar horario"
PB "Acceso: JWT + ADMINISTRADOR | Body: mismos campos del POST"
PB "Response 200: Horario actualizado"
BR
H2 "PATCH /api/schedules/{id}/status - Cambiar estado"
PB "Acceso: JWT + ADMINISTRADOR | Query: status"
PB "Response 200: Horario con nuevo estado"
BR
H2 "DELETE /api/schedules/{id} - Eliminar horario"
PB "Acceso: JWT + ADMINISTRADOR | Response 204 No Content"
BR

# 8. EDUCACION
PB2
H1 "8. MODULO DE CONTENIDO EDUCATIVO (/api/v1/education)"
H2 "GET /api/v1/education - Listar todos los contenidos"
PB "Acceso: Publico | Sin body"
PB "Response 200:"
C '[{"id":1,"title":"Reciclaje Basico","description":"Aprende a reciclar","category":"reciclaje","createdAt":"2026-05-01T10:00:00","files":[{"fileUrl":"/uploads/education/img1.jpg","fileType":"IMAGE"}],"sections":[]}]'
BR
H2 "GET /api/v1/education/{id} - Contenido por ID"
PB "Acceso: Publico"
PB "Response 200: EducationContentResponseDTO completo con files y sections"
PB "Response 404:"
C '{"error":"Contenido no encontrado con id: 99"}'
BR
H2 "GET /api/v1/education/{id}/sections - Secciones de un contenido"
PB "Acceso: Publico"
PB "Response 200:"
C '[{"id":1,"title":"Seccion 1: Tipos de residuos","description":"Los residuos se clasifican en...","files":[{"fileUrl":"/uploads/education/sec1.pdf","fileType":"PDF"}]}]'
BR
H2 "POST /api/v1/education - Crear contenido"
PB "Acceso: JWT + ADMINISTRADOR | Content-Type: multipart/form-data"
PB "Form fields: title (string), description (string), category (string), files[] (archivos .jpg/.png/.pdf)"
PB "Response 201:"
C '{"id":5,"title":"Compostaje en Casa","description":"Aprende compostaje","category":"organico","files":[{"fileUrl":"/uploads/education/1234.jpg","fileType":"IMAGE"}],"sections":[]}'
PB "Response 400 - Sin archivos:"
C '{"error":"Debes enviar al menos un archivo."}'
BR
H2 "PUT /api/v1/education/{id} - Actualizar metadata"
PB "Acceso: JWT + ADMINISTRADOR | Body: {title, description, category}"
PB "Response 200: Contenido actualizado | Response 404: No encontrado"
BR
H2 "DELETE /api/v1/education/{id} - Eliminar contenido"
PB "Acceso: JWT + ADMINISTRADOR"
PB "Response 200:"
C '{"mensaje":"Contenido eliminado correctamente"}'
BR
H2 "POST /api/v1/education/{id}/sections - Agregar seccion"
PB "Acceso: JWT + ADMINISTRADOR | Content-Type: multipart/form-data"
PB "Form fields: title (string), description (string, opcional), files[] (opcional)"
PB "Response 201:"
C '{"id":10,"title":"Nueva Seccion","description":"Contenido adicional","files":[]}'
BR
H2 "PUT /api/v1/education/sections/{sectionId} - Actualizar seccion"
PB "Acceso: JWT + ADMINISTRADOR | Body: {title, description}"
PB "Response 200: Seccion actualizada | Response 404: No encontrada"
BR
H2 "DELETE /api/v1/education/sections/{sectionId} - Eliminar seccion"
PB "Acceso: JWT + ADMINISTRADOR"
PB "Response 200:"
C '{"mensaje":"Seccion eliminada correctamente"}'
BR
H2 "POST /api/v1/education/{id}/feedback - Dar feedback"
PB "Acceso: JWT opcional (si autenticado, registra el email)"
PB "Request Body:"
C '{"useful":true}'
PB "Response 200 - Primer feedback:"
C '{"message":"Feedback registrado"}'
PB "Response 200 - Ya voto:"
C '{"message":"Ya se registro el feedback"}'
BR
H2 "GET /api/v1/education/{id}/feedback/stats - Estadisticas feedback"
PB "Acceso: Publico | Sin body"
PB "Response 200:"
C '{"useful":25,"notUseful":3,"total":28,"usefulPercentage":89.3}'
BR

# 9. QUIZ
PB2
H1 "9. MODULO DE QUIZ (/api/v1/quizzes)"
H2 "GET /api/v1/quizzes/content/{contentId} - Quiz para jugar"
PB "Acceso: JWT opcional"
PB "Response 200:"
C '{"id":1,"title":"Quiz Reciclaje","description":"Pon a prueba tu conocimiento","questions":[{"id":1,"text":"De que color es el contenedor para plastico?","options":["Azul","Verde","Rojo","Amarillo"]}]}'
PB "Response 404: Quiz no encontrado para ese contenido"
BR
H2 "GET /api/v1/quizzes/content/{contentId}/exists - Verificar existencia"
PB "Acceso: JWT opcional"
PB "Response 200:"
C '{"exists":true}'
BR
H2 "POST /api/v1/quizzes/{quizId}/attempt - Enviar respuestas"
PB "Acceso: JWT requerido"
PB "Request Body:"
C '{"answers":[{"questionId":1,"selectedIndex":3},{"questionId":2,"selectedIndex":0}]}'
PB "Response 200:"
C '{"correctAnswers":2,"totalQuestions":2,"pointsEarned":20,"userTotalPoints":170,"firstAttempt":true,"perQuestion":[{"questionId":1,"selectedIndex":3,"correctIndex":3,"wasCorrect":true}],"newAchievements":[]}'
BR
H2 "GET /api/v1/quizzes/me/stats - Mis estadisticas"
PB "Acceso: JWT requerido | Sin body"
PB "Response 200:"
C '{"totalPoints":170,"quizzesCompleted":5,"correctAnswers":38,"totalQuestions":50}'
BR
H2 "POST /api/v1/quizzes/content/{contentId} - Crear quiz (admin)"
PB "Acceso: JWT + ADMINISTRADOR"
PB "Request Body:"
C '{"title":"Quiz Reciclaje","description":"Preguntas de reciclaje","pointsPerQuestion":10,"questions":[{"text":"De que color es el contenedor plastico?","options":["Azul","Verde","Rojo","Amarillo"],"correctIndex":3}]}'
PB "Response 201: QuizAdminResponseDTO con respuestas correctas visibles"
BR
H2 "GET /api/v1/quizzes/content/{contentId}/admin - Ver quiz completo (admin)"
PB "Acceso: JWT + ADMINISTRADOR | Response 200: QuizAdminResponseDTO con correctIndex"
BR
H2 "PUT /api/v1/quizzes/{quizId} - Actualizar quiz (admin)"
PB "Acceso: JWT + ADMINISTRADOR | Body: mismo que POST | Response 200: Quiz actualizado"
BR
H2 "DELETE /api/v1/quizzes/{quizId} - Eliminar quiz (admin)"
PB "Acceso: JWT + ADMINISTRADOR"
PB "Response 200:"
C '{"message":"Quiz eliminado correctamente"}'
BR

# 10. FORO
PB2
H1 "10. MODULO DE FORO COMUNITARIO (/api/forum)"
H2 "GET /api/forum/topics - Listar temas activos"
PB "Acceso: Publico | Sin body"
PB "Response 200:"
C '[{"id":1,"titulo":"Como reciclar en casa","descripcion":"Comparte tus tips de reciclaje","autorNombre":"Juan","fechaCreacion":"2026-05-01T10:00:00","cantidadComentarios":5}]'
BR
H2 "GET /api/forum/topics/{id} - Detalle de tema"
PB "Acceso: Publico"
PB "Response 200:"
C '{"id":1,"titulo":"Como reciclar en casa","descripcion":"Comparte tips...","autorNombre":"Juan","fechaCreacion":"2026-05-01T10:00:00","comentarios":[{"id":1,"texto":"Yo separo plastico y vidrio","usuarioNombre":"Maria","fechaCreacion":"2026-05-01T11:00:00","respuestas":[{"id":1,"texto":"Excelente consejo!","usuarioNombre":"Carlos","fechaCreacion":"2026-05-01T11:30:00"}]}]}'
BR
H2 "POST /api/forum/topics - Crear tema"
PB "Acceso: JWT requerido (autor tomado del JWT)"
PB "Request Body:"
C '{"titulo":"Ideas para reducir residuos","descripcion":"Quiero compartir ideas para reducir la basura en el hogar"}'
PB "Response 201: TopicDetailDTO del tema creado"
BR
H2 "DELETE /api/forum/topics/{id} - Eliminar tema"
PB "Acceso: JWT requerido (solo el autor o ADMINISTRADOR)"
PB "Response 204 No Content | Response 403: No autorizado"
BR
H2 "PATCH /api/forum/topics/{id}/status - Cambiar estado (admin)"
PB "Acceso: JWT + ADMINISTRADOR"
PB "Request Body:"
C '{"estado":"INACTIVO"}'
PB "Response 200: TopicListDTO con estado actualizado"
BR
H2 "GET /api/forum/topics/all - Todos los temas (admin)"
PB "Acceso: JWT + ADMINISTRADOR | Response 200: Array incluyendo ACTIVOS e INACTIVOS"
BR
H2 "GET /api/forum/topics/{topicId}/comments - Comentarios de un tema"
PB "Acceso: Publico"
PB "Response 200:"
C '[{"id":1,"texto":"Yo separo en 4 categorias","usuarioNombre":"Maria","fechaCreacion":"2026-05-01T11:00:00","respuestas":[]}]'
BR
H2 "POST /api/forum/topics/{topicId}/comments - Agregar comentario"
PB "Acceso: JWT requerido"
PB "Request Body:"
C '{"texto":"Excelente tema, yo uso bolsas de colores"}'
PB "Response 201: CommentDTO del comentario creado"
BR
H2 "POST /api/forum/comments/{commentId}/replies - Responder comentario"
PB "Acceso: JWT requerido"
PB "Request Body:"
C '{"texto":"Totalmente de acuerdo contigo!"}'
PB "Response 201: ReplyDTO de la respuesta creada"
BR

# 11. NOTIFICACIONES
PB2
H1 "11. MODULO DE NOTIFICACIONES (/api/notifications)"
H2 "GET /api/notifications/preferences - Obtener preferencias"
PB "Acceso: JWT requerido | Sin body"
PB "Response 200:"
C '{"userId":123456,"emailEnabled":true,"whatsappEnabled":false,"telegramEnabled":false,"whatsappNumber":null,"telegramUsername":null,"advanceHours":24,"organicEnabled":true,"recyclableEnabled":true,"hazardousEnabled":false}'
BR
H2 "PUT /api/notifications/preferences - Actualizar preferencias"
PB "Acceso: JWT requerido"
PB "Request Body:"
C '{"emailEnabled":true,"whatsappEnabled":true,"whatsappNumber":"3001234567","advanceHours":48,"organicEnabled":true,"recyclableEnabled":true}'
PB "Response 200: Preferencias actualizadas"
BR
H2 "GET /api/notifications/history - Historial de notificaciones"
PB "Acceso: JWT requerido | Sin body"
PB "Response 200:"
C '[{"id":1,"title":"Recordatorio recoleccion organicos","message":"Manana pasan a recoger organicos en tu barrio","sentAt":"2026-05-10T08:00:00","channel":"EMAIL","read":false}]'
BR
H2 "GET /api/notifications/campaigns/active - Campanas activas"
PB "Acceso: Publico | Sin body"
PB "Response 200:"
C '[{"id":1,"title":"Semana del Reciclaje","message":"Participa en los eventos de reciclaje","districtId":3,"districtName":"Chapinero","startDate":"2026-05-10","endDate":"2026-05-17","status":"ACTIVO","notified":false}]'
BR
H2 "GET /api/notifications/campaigns/{id} - Detalle campana"
PB "Acceso: JWT requerido | Response 200: CampaignDTO"
BR
H2 "GET /api/notifications/campaigns - Todas las campanas (admin)"
PB "Acceso: JWT + ADMINISTRADOR | Response 200: Array de CampaignDTO"
BR
H2 "POST /api/notifications/campaigns - Crear campana"
PB "Acceso: JWT + ADMINISTRADOR"
PB "Request Body:"
C '{"title":"Dia de Limpieza","message":"Jornada de limpieza comunitaria este sabado","districtId":3,"startDate":"2026-05-15","endDate":"2026-05-15"}'
PB "Response 201: CampaignDTO con id asignado"
BR
H2 "PUT /api/notifications/campaigns/{id} - Actualizar campana"
PB "Acceso: JWT + ADMINISTRADOR | Body: mismo que POST | Response 200: Campana actualizada"
BR
H2 "PATCH /api/notifications/campaigns/{id}/status - Cambiar estado"
PB "Acceso: JWT + ADMINISTRADOR | Query: status (ACTIVO | INACTIVO)"
PB "Response 200: CampaignDTO con estado actualizado"
BR
H2 "DELETE /api/notifications/campaigns/{id} - Eliminar campana"
PB "Acceso: JWT + ADMINISTRADOR | Response 204 No Content"
BR

# 12. RANKING
PB2
H1 "12. MODULO DE RANKING (/api/ranking)"
H2 "GET /api/ranking - Top usuarios por puntos"
PB "Acceso: Publico | Query: limit (int, default 50)"
PB "Ejemplo: /api/ranking?limit=10"
PB "Response 200:"
C '[{"position":1,"names":"Ana","lastName":"Ruiz","nickName":"anar","photo":"/uploads/profile_photo/1.jpg","points":520,"neighborhoodName":"Chapinero"},{"position":2,"names":"Carlos","lastName":"Lopez","nickName":"carlol","photo":null,"points":480,"neighborhoodName":"Teusaquillo"}]'
BR

# 13. GEOGRAFIA
H1 "13. MODULO DE GEOGRAFIA (/api/geography)"
H2 "GET /api/geography/cities - Listar ciudades"
PB "Acceso: Publico | Sin body"
PB "Response 200:"
C '[{"cityId":1,"name":"Bogota"},{"cityId":2,"name":"Medellin"}]'
BR
H2 "GET /api/geography/neighborhoods - Listar barrios"
PB "Acceso: Publico | Query: districtId (int, opcional)"
PB "Ejemplo: /api/geography/neighborhoods?districtId=3"
PB "Response 200:"
C '[{"neighborhoodId":1,"name":"Chapinero Alto"},{"neighborhoodId":2,"name":"Chapinero Central"}]'
BR
H2 "GET /api/geography/neighborhoods/{id}/location - Barrio con localidad y ciudad"
PB "Acceso: Publico | Path: id (int)"
PB "Response 200:"
C '{"neighborhoodId":1,"neighborhoodName":"Chapinero Alto","districtId":3,"districtName":"Chapinero","cityId":1,"cityName":"Bogota"}'
PB "Response 404:"
C '{"status":404,"error":"Not Found","message":"Barrio no encontrado: 99"}'
BR

# 14. RESPUESTAS DE ERROR ESTANDAR
PB2
H1 "14. ESTRUCTURA ESTANDAR DE ERRORES"
P "Todos los errores siguen el siguiente formato JSON:"
C '{"timestamp":"2026-05-11T10:00:00","status":400,"error":"Bad Request","message":"Descripcion del error","path":"/api/endpoint"}'
BR
PB "Codigos de respuesta HTTP utilizados:"
P "  200 OK              - Solicitud exitosa"
P "  201 Created         - Recurso creado correctamente"
P "  204 No Content      - Eliminacion exitosa"
P "  400 Bad Request     - Datos invalidos, campos faltantes o logica de negocio"
P "  401 Unauthorized    - Token JWT no enviado, invalido o expirado"
P "  403 Forbidden       - Rol insuficiente para acceder al recurso"
P "  404 Not Found       - Recurso no encontrado en la base de datos"
P "  500 Internal Server Error - Error interno no controlado"
BR

# FOOTER
$sel.Font.Size=10;$sel.Font.Bold=$false;$sel.Font.Italic=$true;$sel.Font.Color=0;$sel.Font.Name="Calibri"
$sel.TypeText("Documento generado automaticamente - Sistema de Gestion de Residuos - Mayo 2026");$sel.TypeParagraph()

$doc.SaveAs2([ref]$docPath)
$word.Quit()
Write-Host "DOCUMENTO GENERADO EN: $docPath"

