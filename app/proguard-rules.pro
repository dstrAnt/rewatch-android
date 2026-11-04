# Firestore reads and writes this class through reflection.
-keep class it.polimi.rewatch.model.TrackingMediaDto { *; }

# app/build.gradle.kts excludes these Google libraries on purpose.
-dontwarn com.google.android.gms.auth.api.phone.SmsRetriever
-dontwarn com.google.android.gms.auth.api.phone.SmsRetrieverClient
-dontwarn com.google.android.play.core.integrity.IntegrityManagerFactory
-dontwarn com.google.android.play.core.integrity.StandardIntegrityException
-dontwarn com.google.android.play.core.integrity.StandardIntegrityManager
-dontwarn com.google.android.play.core.integrity.StandardIntegrityManager$PrepareIntegrityTokenRequest
-dontwarn com.google.android.play.core.integrity.StandardIntegrityManager$PrepareIntegrityTokenRequest$Builder
-dontwarn com.google.android.play.core.integrity.StandardIntegrityManager$StandardIntegrityToken
-dontwarn com.google.android.play.core.integrity.StandardIntegrityManager$StandardIntegrityTokenProvider
-dontwarn com.google.android.play.core.integrity.StandardIntegrityManager$StandardIntegrityTokenRequest
-dontwarn com.google.android.play.core.integrity.StandardIntegrityManager$StandardIntegrityTokenRequest$Builder
