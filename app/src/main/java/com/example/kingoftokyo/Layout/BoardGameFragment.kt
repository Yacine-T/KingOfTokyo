package com.example.kingoftokyo.Layout

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.kingoftokyo.Entity.Player
import com.example.kingoftokyo.R
import com.example.kingoftokyo.ViewModel.GameState
import com.example.kingoftokyo.ViewModel.GameViewModel


class BoardGameFragment : Fragment() {
    private lateinit var viewModel: GameViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_board_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(GameViewModel::class.java)

        viewModel.gameState.observe(viewLifecycleOwner, { gameState ->
            updateUI(gameState)
        })

        // Observe dice results
//        viewModel.currentDiceResults.observe(viewLifecycleOwner, Observer { diceResults ->
//            updateDiceResults(diceResults)
//        })

    }

    private fun updateUI(gameState: GameState) {
        // Update player info
        updatePlayerInfo(player_1_pseudo, player_1_life, player_1_energy, gameState.players[0])
        updatePlayerInfo(player_2_pseudo, player_2_life, player_2_energy, gameState.players[1])
        updatePlayerInfo(player_3_pseudo, player_3_life, player_3_energy, gameState.players[2])
        updatePlayerInfo(player_4_pseudo, player_4_life, player_4_energy, gameState.players[3])

        // Update player in Tokyo
        val playerInTokyo = gameState.players.find { it.isInTokyo }
        if (playerInTokyo != null) {
            player_in_tokyo_name.text = playerInTokyo.name
            player_in_tokyo_avatar.setImageResource(playerInTokyo.imageResId)
        } else {
            player_in_tokyo_name.text = "No one in Tokyo"
            player_in_tokyo_avatar.setImageResource(R.drawable.default_image)
        }

        // Check for game winner
        gameState.winner?.let { winner ->
            println("Youpi t'as gagner")
        }
    }

    private fun updatePlayerInfo(pseudoTextView: TextView, lifeTextView: TextView, energyTextView: TextView, player: Player) {
        pseudoTextView.text = player.name
        lifeTextView.text = "${player.health} HP"
        energyTextView.text = "${player.energy} Energy"
    }


}