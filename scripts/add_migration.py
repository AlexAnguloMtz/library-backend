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
    if len(sys.argv) != 3:
        print("Usage: add_migration.py V<number> migration_name")
        sys.exit(1)

    version_arg = sys.argv[1]
    name_arg = sys.argv[2]

    if not version_arg.startswith("V") or not version_arg[1:].isdigit():
        print("Error: version must be in format V<number> (e.g. V3)")
        sys.exit(1)

    target_version = int(version_arg[1:])

    if not os.path.exists(MIGRATIONS_DIR):
        print(f"Error: migrations directory '{MIGRATIONS_DIR}' does not exist")
        sys.exit(1)

    migrations = get_existing_migrations()

    if not migrations:
        # case 1: empty folder
        if target_version != 1:
            print("Error: No migrations yet. First migration must be V1.")
            sys.exit(1)
        else:
            filename = f"V{target_version}__{name_arg}.sql"
            path = os.path.join(MIGRATIONS_DIR, filename)
            open(path, "w").close()
            print(f"Created {filename}")
            return

    max_version = migrations[-1][0]

    # case 2: requested version > max+1
    if target_version > max_version + 1:
        print(
            f"Error: Cannot add V{target_version}. "
            f"Highest version is V{max_version}. You must add V{max_version+1}."
        )
        sys.exit(1)

    # case 3: append at the end
    if target_version == max_version + 1:
        filename = f"V{target_version}__{name_arg}.sql"
        path = os.path.join(MIGRATIONS_DIR, filename)
        open(path, "w").close()
        print(f"Created {filename}")
        return

    # case 4: shift existing migrations >= target_version
    to_shift = [(v, f) for v, f in migrations if v >= target_version]
    # rename in descending order to avoid collisions
    for v, f in sorted(to_shift, key=lambda x: -x[0]):
        new_name = f"V{v+1}__{f.split('__', 1)[1]}"
        old_path = os.path.join(MIGRATIONS_DIR, f)
        new_path = os.path.join(MIGRATIONS_DIR, new_name)
        shutil.move(old_path, new_path)
        print(f"Renamed {f} -> {new_name}")

    # now insert the new migration
    filename = f"V{target_version}__{name_arg}.sql"
    path = os.path.join(MIGRATIONS_DIR, filename)
    open(path, "w").close()
    print(f"Created {filename}")


if __name__ == "__main__":
    main()
