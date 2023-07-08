package kr.weit.odya.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader

@Configuration
class FirebaseAuthConfig(
    private val resourceLoader: ResourceLoader,
) {
    @Bean
    fun firebaseApp(): FirebaseApp {
        val serviceAccount =
            resourceLoader.getResource("classpath:/firebase/odya-2f0f1-firebase-adminsdk-pwfxi-18bc9d3347.json")

        val options: FirebaseOptions = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount.inputStream))
            .build()

        return FirebaseApp.initializeApp(options)
    }

    @Bean
    fun firebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance(firebaseApp())
}
