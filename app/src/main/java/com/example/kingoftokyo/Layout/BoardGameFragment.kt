package com.example.kingoftokyo.Layout

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        super.onViewCreated(view, savedInstanceState)

        val player1Pseudo: TextView = view.findViewById(R.id.player_1_pseudo)
        val player1Life: TextView = view.findViewById(R.id.player_1_life)
        val player1Energy: TextView = view.findViewById(R.id.player_1_energy)

        val player2Pseudo: TextView = view.findViewById(R.id.player_2_pseudo)
        val player2Life: TextView = view.findViewById(R.id.player_2_life)
        val player2Energy: TextView = view.findViewById(R.id.player_2_energy)

        val player3Pseudo: TextView = view.findViewById(R.id.player_3_pseudo)
        val player3Life: TextView = view.findViewById(R.id.player_3_life)
        val player3Energy: TextView = view.findViewById(R.id.player_3_energy)

        val player4Pseudo: TextView = view.findViewById(R.id.player_4_pseudo)
        val player4Life: TextView = view.findViewById(R.id.player_4_life)
        val player4Energy: TextView = view.findViewById(R.id.player_4_energy)

        // Observe changes in the game state
        viewModel.gameState.observe(viewLifecycleOwner) { gameState ->

            val players = gameState.players
            if (players.isNotEmpty()) {
                player1Pseudo.text = players[0].name
                player1Life.text = players[0].health.toString()
                player1Energy.text = players[0].energy.toString()

                player2Pseudo.text = players[1].name
                player2Life.text = players[1].health.toString()
                player2Energy.text = players[1].energy.toString()

                player3Pseudo.text = players[2].name
                player3Life.text = players[2].health.toString()
                player3Energy.text = players[2].energy.toString()

                player4Pseudo.text = players[3].name
                player4Life.text = players[3].health.toString()
                player4Energy.text = players[3].energy.toString()

            }

            // Check if there's a player in Tokyo and update the UI
            val playerInTokyo = players.find { it.isInTokyo }
            if (playerInTokyo != null) {
                val playerInTokyoName: TextView = view.findViewById(R.id.player_in_tokyo_name)
                playerInTokyoName.text = playerInTokyo.name

            }
        }

    }
}
