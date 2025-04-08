package com.apolo.wms.operaciones.entrada.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabsPagerAdapterEntrada(fm: FragmentManager, lifecycle: Lifecycle, private var numberOfTabs: Int) : FragmentStateAdapter(fm, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                val entradaConferenciaFragment = EntradaConferenciaFragment()
                return entradaConferenciaFragment
            }
            1 -> {
                val entradaConferidoFragment = EntradaConferidoFragment()
                return entradaConferidoFragment
            }
            else -> return DemoFragment()
        }
    }

    override fun getItemCount(): Int {
        return numberOfTabs
    }
}