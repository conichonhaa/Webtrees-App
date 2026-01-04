# Webtrees Mobile App

Application Android pour accéder à votre arbre généalogique Webtrees de manière optimisée pour mobile.

## 📱 Fonctionnalités

- ✅ Configuration initiale en 3 étapes (URL, identifiants, arbre)
- ✅ Interface mobile moderne avec Material Design
- ✅ Injection CSS personnalisée pour une expérience mobile optimale
- ✅ Navigation par bottom bar
- ✅ Gestion de plusieurs arbres généalogiques
- ✅ Cache et déconnexion
- ✅ Design responsive et touch-friendly

## 🚀 Installation

### Prérequis
- Android Studio Arctic Fox ou plus récent
- JDK 8 ou plus récent
- Android SDK (API 24 minimum)

### Étapes d'installation

1. **Extraire le projet**
   - Décompressez le fichier ZIP
   - Ouvrez Android Studio

2. **Importer le projet**
   - File → Open
   - Sélectionnez le dossier `WebtreesApp`
   - Attendez la synchronisation Gradle

3. **Configurer le package name (optionnel)**
   - Remplacez `com.votredomaine.webtrees` par votre propre package
   - Dans AndroidManifest.xml et tous les fichiers Kotlin

4. **Compiler et lancer**
   - Cliquez sur le bouton ▶️ Run
   - Sélectionnez votre appareil/émulateur

## 📝 Utilisation

### Premier lancement
1. Entrez l'URL de votre site Webtrees
2. Saisissez vos identifiants
3. Sélectionnez votre arbre généalogique par défaut

### Navigation
- **Accueil** : Vue de l'arbre principal
- **Recherche** : Rechercher des personnes
- **Stats** : Statistiques et rapports
- **Profil** : Liste des individus

## 🎨 Personnalisation

### Modifier le CSS injecté
Ouvrez `WebViewManager.kt` et modifiez la fonction `injectCustomCSS()` pour ajuster les styles selon vos besoins.

### Changer les couleurs
Modifiez `res/values/colors.xml` pour personnaliser les couleurs de l'application.

## 📦 Structure du projet

```
WebtreesApp/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/votredomaine/webtrees/
│   │       │   ├── MainActivity.kt
│   │       │   ├── SetupActivity.kt
│   │       │   ├── WebViewManager.kt
│   │       │   └── ...
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   ├── drawable/
│   │       │   ├── values/
│   │       │   └── menu/
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## 🔧 Dépannage

### Erreur de compilation
1. File → Invalidate Caches → Invalidate and Restart
2. Build → Clean Project
3. Build → Rebuild Project

### L'app crash au lancement
Vérifiez les logs dans Logcat et assurez-vous que :
- Les permissions Internet sont bien dans AndroidManifest.xml
- L'URL entrée est valide

## 📄 Licence

Ce projet est fourni tel quel pour un usage personnel.

## 👨‍💻 Support

Pour toute question ou problème, veuillez consulter la documentation de Webtrees ou créer une issue.

## 🎯 Améliorations futures

- [ ] Authentification automatique
- [ ] Mode hors ligne avec cache
- [ ] Notifications push pour anniversaires
- [ ] Partage de profils
- [ ] Mode sombre
- [ ] Support multi-langues
