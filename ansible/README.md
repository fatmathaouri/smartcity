# Ansible — SmartCity VPS

- **Control node** : VM master (venv `~/.venv-ansible`, ansible-core 2.17)
- **Managed node** : VPS `137.74.114.42` (ubuntu, sudo sans mot de passe, clé `~/.ssh/ansible_vps`)

## Prérequis

```bash
source ~/.venv-ansible/bin/activate   # obligatoire à chaque session (l'ansible apt 2.10 est cassé)
```

L'inventaire versionné ici (`inventory.ini`) est identique à `~/smartcity-infra/inventory.ini`.

## Exécution

```bash
cd ~/smartcity/ansible
ansible-playbook --syntax-check site.yml
ansible-playbook --check --diff site.yml    # simulation partielle (git/compose non simulés)
ansible-playbook site.yml                   # run 1
ansible-playbook site.yml                   # run 2 → "changed=0" = idempotent
```

## Rôles

| Rôle | Action |
|---|---|
| `common` | `python3-apt` si absent, `curl/git/ca-certificates`, sysctl `vm.max_map_count=262144`, swap 2 Go |
| `docker` | Docker + plugin compose (sauté si déjà présent), service actif, `ubuntu` dans le groupe `docker` |
| `deploy` | `git pull` (branche `master`) puis `docker compose -f docker-compose.prod.yaml pull` + `up -d` |
| `maintenance` | `~/backups/`, script `~/bin/backup-mysql.sh` (mysqldump via `docker exec`, purge > 7 j), cron 03:00 |

## Hors périmètre (configuré manuellement — ne pas toucher)

- **nginx host + TLS** sur :80/:443 → `app.newgencity.com` / `api.newgencity.com`
- **SonarQube** sur :9000 (`~/sonarqube`)
- **pg18** (conteneur orphelin sans rapport avec SmartCity)

## Migration initiale (une seule fois, sur le VPS)

Avant le 1er run : sauvegarder les éditions locales, repasser l'arbre git sur `origin/master`,
supprimer la ligne cron + le script backup legacy (le rôle `maintenance` les recrée proprement) :

```bash
cd ~/smartcity
mkdir -p ~/smartcity-local-backup-20260930
cp -a frontend/nginx.conf docker-compose.yaml smartcity/mvnw \
      smartcity/src/main/java/tn/esprit/spring/smartcity/config/CorsConfig.java \
      ~/smartcity-local-backup-20260930/
ls -la ~/smartcity-local-backup-20260930/
git status -sb && crontab -l
# --- après contrôle de la sauvegarde ---
git fetch origin
git checkout -- frontend/nginx.conf docker-compose.yaml smartcity/mvnw \
                 smartcity/src/main/java/tn/esprit/spring/smartcity/config/CorsConfig.java
git merge --ff-only origin/master
git status -sb
crontab -l | grep -v backup-mysql.sh | crontab -
rm -f ~/smartcity/backup-mysql.sh
```

Note : `docker-compose.yaml` (dev/CI, avec `build:`) est remplacé côté prod par
`docker-compose.prod.yaml` ; le déploiement passe **toujours** par `docker compose -f docker-compose.prod.yaml`.
