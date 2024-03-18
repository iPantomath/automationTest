package com.dhl.demp.dmac.ext

import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Suppress("unused")
inline fun <reified T: ViewBinding> Fragment.viewBinding(): ReadOnlyProperty<Fragment, T> =
        FragmentViewBindingDelegate(FragmentViewBinder(T::class.java)::bind)

class FragmentViewBindingDelegate<T : ViewBinding>(
        private val viewBindingFactory: (View) -> T
) : ReadOnlyProperty<Fragment, T> {
    private var binding: T? = null
    private val lifecycleObserver = BindingLifecycleObserver()

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        check(Looper.myLooper() == Looper.getMainLooper())

        binding?.let {
            return it
        }

        val lifecycle = thisRef.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
        }

        val view = thisRef.requireView()
        thisRef.viewLifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        return viewBindingFactory(view).also { this.binding = it }
    }

    private inner class BindingLifecycleObserver : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            owner.lifecycle.removeObserver(this)
            binding = null
        }
    }
}

class FragmentViewBinder<T : ViewBinding>(private val viewBindingClass: Class<T>) {
    /**
     * Cache static method `ViewBinding.bind(View)`
     */
    private val bindViewMethod by lazy(LazyThreadSafetyMode.NONE) {
        viewBindingClass.getMethod("bind", View::class.java)
    }

    /**
     * Create new [ViewBinding] instance
     */
    @Suppress("UNCHECKED_CAST")
    fun bind(view: View): T {
        return bindViewMethod(null, view) as T
    }
}