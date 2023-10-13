package com.example.kingoftokyo.Layout

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.example.kingoftokyo.R
import androidx.navigation.fragment.findNavController

class MenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val playButton : Button = view.findViewById(R.id.playButton)
        playButton.setOnClickListener {
            println("menu")
            findNavController().navigate(R.id.action_menuFragment_to_boardGameFragment)
        }
    }

}
