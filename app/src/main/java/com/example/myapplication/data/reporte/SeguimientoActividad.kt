package com.example.myapplication.data.reporte

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

// Recuerda cuál es la pantalla abierta para lanzar WhatsApp desde ella.
// Así WhatsApp se abre dentro de la misma tarea y el botón atrás regresa a AutoClima.
class SeguimientoActividad : Application.ActivityLifecycleCallbacks {
    private var referencia: WeakReference<Activity>? = null

    // La pantalla visible ahora, o null si la app no está al frente.
    fun actual(): Activity? = referencia?.get()

    override fun onActivityResumed(activity: Activity) { referencia = WeakReference(activity) }
    override fun onActivityPaused(activity: Activity) { if (referencia?.get() === activity) referencia = null }
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {} }
