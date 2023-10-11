package com.example.kingoftokyo.Layout

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.example.kingoftokyo.R
import com.example.kingoftokyo.ViewModel.GameViewModel

class BoardGameFragment : Fragment() {

    private val viewModel: GameViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_board_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.gameState.observe(viewLifecycleOwner) { gameState ->
            for (i in 1..4) {
                val pseudo = view.findViewById<TextView>(
                    resources.getIdentifier(
                        "player_" + i + "_pseudo",
                        "id",
                        requireContext().packageName
                    )
                )
                val energy = view.findViewById<TextView>(
                    resources.getIdentifier(
                        "player_" + i + "_energy",
                        "id",
                        requireContext().packageName
                    )
                );
                val life = view.findViewById<TextView>(
                    resources.getIdentifier(
                        "player_" + i + "_life",
                        "id",
                        requireContext().packageName
                    )
                );

                pseudo.text = viewModel.players[i - 1].name;
                energy.text = viewModel.players[i - 1].energy.toString();
                life.text = viewModel.players[i - 1].health.toString();
            }
            val playerInTokyo = gameState.players.find { it.isInTokyo }
            if(playerInTokyo !== null) {
                val playerInTokyoName = view.findViewById<TextView>(R.id.player_in_tokyo_name);
                val playerInTokyoAvatar = view.findViewById<ImageView>(R.id.player_in_tokyo_avatar);
                playerInTokyoName.text = playerInTokyo.name;
            }
        }
    }

}
