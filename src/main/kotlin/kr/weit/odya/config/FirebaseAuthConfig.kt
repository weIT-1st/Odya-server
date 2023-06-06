package kr.weit.odya.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.FileInputStream

@Configuration
class FirebaseAuthConfig {
    @Bean
    fun firebaseApp(): FirebaseApp {
        val serviceAccount =
            FileInputStream(ClassPathResource("firebase/odya-2f0f1-firebase-adminsdk-pwfxi-18bc9d3347.json").file)

        val options: FirebaseOptions = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        return FirebaseApp.initializeApp(options)
    }

    @Bean
    fun firebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance(firebaseApp())
}
