import os
import zipfile

source_dir = "/mnt/almacen/.minecraft/home/NeoForge 1.21.1"
output_zip = "/home/owldsee/cliente-quequeworld.zip"
parent_folder_in_zip = "cliente-quequeworld"

folders_to_include = ['mods', 'config', 'defaultconfigs', 'fancymenu_data', 'resourcepacks', 'shaderpacks']
files_to_include = ['options.txt']

# Lista de palabras clave para excluir mods de grabación/cámara/replay
recording_keywords = ['replay', 'playmod', 'freecam', 'free_cam', 'free-cam']

print(f"Iniciando la compresión de carpetas desde: {source_dir}")
print(f"Guardando zip en: {output_zip}")
print(f"Estructura del zip anidada bajo la carpeta principal: '{parent_folder_in_zip}/'")

# Eliminar el zip anterior si existe para evitar duplicación
if os.path.exists(output_zip):
    print("Eliminando archivo zip anterior...")
    os.remove(output_zip)

with zipfile.ZipFile(output_zip, 'w', zipfile.ZIP_DEFLATED) as zipf:
    # 1. Comprimir las carpetas especificadas
    for folder in folders_to_include:
        folder_path = os.path.join(source_dir, folder)
        if not os.path.exists(folder_path):
            print(f"Advertencia: No se encontró la carpeta {folder}")
            continue
            
        print(f"Procesando carpeta: {folder}...")
        for root, dirs, files in os.walk(folder_path):
            for file in files:
                file_abs_path = os.path.join(root, file)
                
                # Si estamos en la carpeta mods, filtrar los mods de grabación
                if folder == 'mods':
                    file_lower = file.lower()
                    if any(kw in file_lower for kw in recording_keywords):
                        print(f"   -> EXCLUIDO (Mod de grabación/replay): {file}")
                        continue
                
                # Calcular la ruta relativa dentro del zip y prefijarla con la carpeta contenedora
                rel_path = os.path.relpath(file_abs_path, source_dir)
                archive_name = os.path.join(parent_folder_in_zip, rel_path)
                zipf.write(file_abs_path, archive_name)

    # 2. Comprimir los archivos sueltos especificados (como options.txt)
    for file_name in files_to_include:
        file_path = os.path.join(source_dir, file_name)
        if os.path.exists(file_path):
            print(f"Comprimiendo archivo suelto: {file_name}...")
            archive_name = os.path.join(parent_folder_in_zip, file_name)
            zipf.write(file_path, archive_name)
        else:
            print(f"Advertencia: No se encontró el archivo suelto {file_name}")

print("¡Compresión completada exitosamente!")
