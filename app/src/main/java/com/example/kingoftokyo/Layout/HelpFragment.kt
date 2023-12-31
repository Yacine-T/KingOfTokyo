package com.example.kingoftokyo.Layout

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.kingoftokyo.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HelpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HelpFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val backToMenuFragment: Button = view.findViewById(R.id.help_back_to_menu_btn)

        backToMenuFragment.setOnClickListener{
            findNavController().navigate(R.id.action_helpFragment_to_menuFragment)
        }

        val helpLink: TextView = view.findViewById(R.id.help_link)
        helpLink.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://www.regledujeu.fr/king-of-tokyo/")
            startActivity(intent)
        }
    }
}