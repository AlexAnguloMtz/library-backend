### 📊 Matriz de permisos. Módulo 'usuarios'.

|                | **A sí mismo**                                                     | **Administrador**                                      | **Bibliotecario**                                       | **Usuario**                                            |
|----------------|--------------------------------------------------------------------|--------------------------------------------------------|---------------------------------------------------------|--------------------------------------------------------|
| **Administrador** | ✅ Leer<br> ✅ Editar<br> ❌ Borrar<br> .............................. | ✅ Leer<br> ❌ Editar<br> ❌ Borrar<br> ✅ Asignar este rol | ✅ Leer<br> ✅ Editar<br> ✅ Borrar<br> ✅ Asignar este rol | ✅ Leer<br> ✅ Editar<br> ✅ Borrar<br> ✅ Asignar este rol |
| **Bibliotecario** | ✅ Leer<br> ❌ Editar<br> ❌ Borrar<br> .............................. | ❌ Leer<br> ❌ Editar<br> ❌ Borrar<br> ❌ Asignar este rol | ❌ Leer<br> ❌ Editar<br> ❌ Borrar<br> ❌ Asignar este rol | ✅ Leer<br> ✅ Editar<br> ✅ Borrar<br> ✅ Asignar este rol |
| **Usuario**       | ✅ Leer<br> ❌ Editar<br> ❌ Borrar<br> .............................. | ❌ Leer<br> ❌ Editar<br> ❌ Borrar<br> ❌ Asignar este rol | ❌ Leer<br> ❌ Editar<br> ❌ Borrar<br> ❌ Asignar este rol | ❌ Leer<br> ❌ Editar<br> ❌ Borrar<br> ❌ Asignar este rol |
