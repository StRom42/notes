package ru.itmo.notes.common

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import ru.itmo.notes.R


fun addFragment(fragmentManager: FragmentManager, fragment: Fragment, istransition:Boolean){
    val fragmentTransition = fragmentManager.beginTransaction()

    if (istransition){
        fragmentTransition.setCustomAnimations(android.R.anim.slide_out_right,android.R.anim.slide_in_left)
    }
    fragmentTransition.add(R.id.frame_layout,fragment).addToBackStack(fragment.javaClass.simpleName).commit()
}

fun replaceFragment(fragmentManager: FragmentManager, fragment: Fragment, istransition:Boolean){
    val fragmentTransition = fragmentManager.beginTransaction()

    if (istransition){
        fragmentTransition.setCustomAnimations(android.R.anim.slide_out_right,android.R.anim.slide_in_left)
    }
    fragmentTransition.replace(R.id.frame_layout,fragment).addToBackStack(fragment.javaClass.simpleName).commit()
}

fun alert(context: Context, text: String) {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
}