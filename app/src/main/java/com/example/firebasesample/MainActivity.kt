package com.example.firebasesample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初期化
        auth = Firebase.auth
        functions = Firebase.functions

    }

    public override fun onStart() {
        super.onStart()
        signInAnonymously()

        // ログイン情報のチェック
        val currentUser = auth.currentUser
        updateUI(currentUser)

        val text = findViewById<TextView>(R.id.hello_world)
        val button = findViewById<Button>(R.id.setCustomClaim)
        button.setOnClickListener {
            text.text = "cloud functions リクエスト中"

            // cloud functions を呼び出して内容をテキストに表示
            getCloudFunction("setCustomClaim")
                .addOnCompleteListener(OnCompleteListener { task ->
                    // 失敗
                    if (!task.isSuccessful) {
                        Log.w(TAG, "addMessage:onFailure", task.exception)
                        text.text = "error"
                        return@OnCompleteListener
                    }

                    // 成功
                    val result = task.result.toString()
                    text.text = result
                })

            // 認証情報の更新
            currentUser?.getIdToken(true)
        }


        val buttonHelloWorld = findViewById<Button>(R.id.buttonHelloWorld)
        buttonHelloWorld.setOnClickListener {
            text.text = "cloud functions リクエスト中"

            // cloud functions を呼び出して内容をテキストに表示
            getCloudFunction("helloWorld")
                .addOnCompleteListener(OnCompleteListener { task ->
                    // 失敗
                    if (!task.isSuccessful) {
                        Log.w(TAG, "addMessage:onFailure", task.exception)
                        text.text = "error"
                        return@OnCompleteListener
                    }

                    // 成功
                    val result = task.result
                    text.text = result
                })
        }

        val getCustomClaim = findViewById<Button>(R.id.getCustomClaim)
        getCustomClaim.setOnClickListener {
            text.text = "cloud functions リクエスト中"
            // cloud functions を呼び出して内容をテキストに表示
            getCloudFunction("getCustomClaim")
                .addOnCompleteListener(OnCompleteListener { task ->
                    // 失敗
                    if (!task.isSuccessful) {
                        Log.w(TAG, "addMessage:onFailure", task.exception)
                        text.text = "error"
                        return@OnCompleteListener
                    }

                    // 成功
                    val result = task.result.toString()
                    text.text = result
                })

            // 認証情報の更新
            currentUser?.getIdToken(true)?.addOnSuccessListener {
                // 自分のclaims["admin"]をトースト表示してみる
                Toast.makeText(baseContext, "Authから取得したCustomClaim ownerId:${it.claims["ownerId"].toString()}",
                    Toast.LENGTH_SHORT).show()
            }
        }

    }

    // 匿名ログイン
    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInAnonymously:success")
                    val user = auth.currentUser
                    Log.d(TAG, "currentUser:${user?.uid}")
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInAnonymously:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {

    }

    // cloud functions サンプル関数を呼び出し
    private fun getCloudFunction(name: String): Task<String> {
        val data = hashMapOf(
            "ownerId" to "testData"
        )
        return functions.getHttpsCallable(name)
            .call(data)
//            .call()
            .continueWith { task ->
                val result = task.result?.data as String // json形式で受け取った keyがresultのdataをStringに変換してる？？？
                result
            }
    }

    companion object {
        private const val TAG = "AnonymousAuth"
    }

}

// 公式ドキュメント: https://firebase.google.com/docs/auth/android/anonymous-auth?hl=ja
// コピペ元コード: https://github.com/firebase/snippets-android/blob/8184cba2c40842a180f91dcfb4a216e721cc6ae6/auth/app/src/main/java/com/google/firebase/quickstart/auth/kotlin/AnonymousAuthActivity.kt