#!/usr/bin/env python3
import os
import sys
import re
import shutil

MIGRATIONS_DIR = os.path.join(os.path.dirname(__file__), "../migrations")
MIGRATION_PATTERN = re.compile(r"^V(\d+)__.+\.sql$")

def get_existing_migrations():
    files = os.listdir(MIGRATIONS_DIR)
    migrations = []
    for f in files:
        match = MIGRATION_PATTERN.match(f)
        if match:
            version = int(match.group(1))
            migrations.append((version, f))
    return sorted(migrations, key=lambda x: x[0])


def main():
    if len(sys.argv) != 2:
        print("Usage: delete_migration.py V<number>")
        sys.exit(1)

    version_arg = sys.argv[1]

    if not version_arg.startswith("V") or not version_arg[1:].isdigit():
        print("Error: version must be in format V<number> (e.g. V3)")
        sys.exit(1)

    target_version = int(version_arg[1:])

    if not os.path.exists(MIGRATIONS_DIR):
        print(f"Error: migrations directory '{MIGRATIONS_DIR}' does not exist")
        sys.exit(1)

    migrations = get_existing_migrations()

    if not migrations:
        print("Error: no migrations found.")
        sys.exit(1)

    # buscar si existe la version a borrar
    migration_to_delete = None
    for v, f in migrations:
        if v == target_version:
            migration_to_delete = (v, f)
            break

    if not migration_to_delete:
        print(f"Error: migration V{target_version} not found.")
        sys.exit(1)

    # borrar el archivo
    os.remove(os.path.join(MIGRATIONS_DIR, migration_to_delete[1]))
    print(f"Deleted {migration_to_delete[1]}")

    # shift down todas las versiones mayores
    to_shift = [(v, f) for v, f in migrations if v > target_version]
    for v, f in sorted(to_shift, key=lambda x: x[0]):
        new_name = f"V{v-1}__{f.split('__', 1)[1]}"
        old_path = os.path.join(MIGRATIONS_DIR, f)
        new_path = os.path.join(MIGRATIONS_DIR, new_name)
        shutil.move(old_path, new_path)
        print(f"Renamed {f} -> {new_name}")


if __name__ == "__main__":
    main()
