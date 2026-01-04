# ✅ CHECKLIST D'INSTALLATION WEBTREES APP

## 📋 Avant de commencer

- [ ] Android Studio installé (version Hedgehog ou plus récent)
- [ ] JDK 17 installé
- [ ] Connexion Internet active

---

## 🆕 ÉTAPE 1 : Création du projet

- [ ] Ouvrir Android Studio
- [ ] File → New → New Project
- [ ] Sélectionner "Empty Views Activity"
- [ ] Configurer:
  - [ ] Name: `WebtreesApp`
  - [ ] Package name: `com.votrenom.webtrees`
  - [ ] Save location: Choisir un dossier
  - [ ] Language: `Kotlin`
  - [ ] Minimum SDK: `API 24 (Android 7.0)`
  - [ ] Build configuration: `Kotlin DSL`
- [ ] Cliquer "Finish"
- [ ] Attendre la fin de la création (barre de progression en bas)

---

## 📁 ÉTAPE 2 : Création des packages

Dans `app/src/main/java/com/votrenom/webtrees/`:

- [ ] Clic droit → New → Package → `activities`
- [ ] Clic droit → New → Package → `adapters`
- [ ] Clic droit → New → Package → `models`
- [ ] Clic droit → New → Package → `utils`

---

## 📝 ÉTAPE 3 : Copie des fichiers Kotlin

### Activities (dans le package activities/)
- [ ] SplashActivity.kt
- [ ] OnboardingUrlActivity.kt
- [ ] OnboardingLoginActivity.kt
- [ ] OnboardingTreeActivity.kt
- [ ] MainActivity.kt

### Adapters (dans le package adapters/)
- [ ] TreeAdapter.kt

### Models (dans le package models/)
- [ ] Tree.kt

### Utils (dans le package utils/)
- [ ] PreferencesManager.kt
- [ ] CssInjector.kt

---

## 🎨 ÉTAPE 4 : Création des dossiers ressources

Dans `app/src/main/res/`:

- [ ] Vérifier que `layout/` existe
- [ ] Créer `menu/` si absent (Clic droit sur res → New → Android Resource Directory → Resource type: menu)

---

## 📄 ÉTAPE 5 : Copie des fichiers XML

### Layouts (dans res/layout/)
- [ ] activity_splash.xml
- [ ] activity_onboarding_url.xml
- [ ] activity_onboarding_login.xml
- [ ] activity_onboarding_tree.xml
- [ ] item_tree.xml
- [ ] activity_main.xml
- [ ] nav_header.xml

### Menus (dans res/menu/)
- [ ] bottom_navigation_menu.xml
- [ ] drawer_menu.xml

### Values (dans res/values/)
- [ ] strings.xml (remplacer le contenu)
- [ ] colors.xml (remplacer le contenu)
- [ ] themes.xml (remplacer le contenu)

### Drawables (dans res/drawable/)
- [ ] gradient_background.xml

---

## 🎯 ÉTAPE 6 : Création des icônes

Pour chaque icône, faire:
1. Clic droit sur `res/drawable`
2. New → Vector Asset
3. Clic sur l'icône à côté de "Clip Art"
4. Rechercher et sélectionner
5. Name: taper le nom
6. Next → Finish

Liste des icônes à créer:

- [ ] ic_tree (chercher: "nature" ou "park")
- [ ] ic_person (chercher: "person")
- [ ] ic_search (chercher: "search")
- [ ] ic_stats (chercher: "bar_chart")
- [ ] ic_menu (chercher: "menu")
- [ ] ic_server (chercher: "dns")
- [ ] ic_link (chercher: "link")
- [ ] ic_lock (chercher: "lock")
- [ ] ic_refresh (chercher: "refresh")
- [ ] ic_delete (chercher: "delete")
- [ ] ic_settings (chercher: "settings")
- [ ] ic_logout (chercher: "logout")

---

## ⚙️ ÉTAPE 7 : Configuration Gradle

- [ ] Ouvrir `build.gradle.kts (Module :app)`
- [ ] Remplacer la section `dependencies` par celle fournie dans build.gradle.kts
- [ ] Cliquer sur "Sync Now" (en haut à droite)
- [ ] Attendre la fin de la synchronisation

---

## 📱 ÉTAPE 8 : AndroidManifest.xml

- [ ] Ouvrir `app/src/main/AndroidManifest.xml`
- [ ] Remplacer TOUT le contenu par le fichier fourni dans 09_Manifest/
- [ ] Vérifier que le package correspond: `com.votrenom.webtrees`

---

## 🔨 ÉTAPE 9 : Build du projet

- [ ] Build → Clean Project
- [ ] Attendre la fin
- [ ] Build → Rebuild Project
- [ ] Vérifier qu'il n'y a PAS d'erreurs (onglet Build en bas)

---

## 📱 ÉTAPE 10 : Préparation de l'émulateur

### Option A : Créer un émulateur

- [ ] Tools → Device Manager
- [ ] Create Device
- [ ] Choisir "Pixel 6"
- [ ] Next
- [ ] Télécharger "Android 14.0 (API 34)" si nécessaire
- [ ] Next → Finish

### Option B : Utiliser un téléphone physique

- [ ] Sur le téléphone: Paramètres → À propos
- [ ] Taper 7 fois sur "Numéro de build"
- [ ] Retour → Options développeur
- [ ] Activer "Débogage USB"
- [ ] Connecter le câble USB
- [ ] Autoriser le débogage sur le téléphone

---

## 🚀 ÉTAPE 11 : Lancement de l'app

- [ ] Sélectionner l'appareil dans le menu déroulant en haut
- [ ] Cliquer sur ▶ (Run) ou Shift+F10
- [ ] Attendre l'installation
- [ ] L'app se lance!

---

## ✅ VÉRIFICATIONS FINALES

### À la première ouverture, vous devez voir:

- [ ] Écran splash avec logo et gradient violet
- [ ] Écran "Étape 1/3" pour entrer l'URL
- [ ] Possibilité de taper une URL
- [ ] Bouton "Suivant" cliquable

### Test complet:

- [ ] Entrer `https://webtrees.net/demo-dev/`
- [ ] Cliquer Suivant
- [ ] Entrer username: `guest` ou créer un compte
- [ ] Entrer un mot de passe (ou "guest")
- [ ] Cliquer Suivant
- [ ] Voir la liste des arbres (peut être simulée)
- [ ] Sélectionner un arbre
- [ ] Cliquer Terminer
- [ ] Voir le WebView charger le site Webtrees

---

## 🐛 EN CAS DE PROBLÈME

### Erreurs de compilation

- [ ] Build → Clean Project
- [ ] Build → Rebuild Project
- [ ] File → Invalidate Caches → Invalidate and Restart

### Icônes manquantes

- [ ] Vérifier que TOUTES les icônes de l'étape 6 sont créées
- [ ] Vérifier les noms (ic_tree, ic_person, etc.)

### App crash au lancement

- [ ] Ouvrir Logcat (onglet en bas)
- [ ] Chercher les lignes rouges avec "Exception"
- [ ] Vérifier AndroidManifest.xml
- [ ] Vérifier que toutes les Activities sont déclarées

### WebView blanc

- [ ] Vérifier la permission INTERNET dans AndroidManifest
- [ ] Vérifier l'URL (doit être https://)
- [ ] Tester avec l'URL de démo d'abord

---

## 🎉 SUCCÈS !

Si tout fonctionne:

- [ ] L'app se lance sans crash
- [ ] L'onboarding s'affiche correctement
- [ ] Le WebView charge le site Webtrees
- [ ] La navigation en bas fonctionne
- [ ] Le menu latéral s'ouvre

**Félicitations ! Votre app Webtrees Mobile est prête ! 🚀**

---

## 📝 PROCHAINES ÉTAPES

- [ ] Tester avec votre propre serveur Webtrees
- [ ] Personnaliser les couleurs dans colors.xml
- [ ] Modifier le CSS dans CssInjector.kt
- [ ] Changer l'icône de l'app
- [ ] Implémenter l'authentification réelle (optionnel)

---

## 📊 TEMPS ESTIMÉ

- Débutant: 2-3 heures
- Intermédiaire: 1-2 heures
- Expérimenté: 30-60 minutes

**Bon courage ! 💪**
