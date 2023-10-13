package com.example.kingoftokyo.Layout

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.kingoftokyo.Entity.DiceFace
import com.example.kingoftokyo.Entity.IAPlayer
import com.example.kingoftokyo.Entity.Player
import com.example.kingoftokyo.Entity.UIEvent
import com.example.kingoftokyo.R
import com.example.kingoftokyo.ViewModel.GameViewModel
import java.util.LinkedList
import java.util.Queue


class BoardGameFragment : Fragment() {

    private val viewModel: GameViewModel by viewModels()
    private lateinit var rollDiceButton: Button
    private val dialogQueue: Queue<UIEvent.ShowDialog> = LinkedList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_board_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentPlayer = viewModel.gameState.value?.currentTurnPlayer
        if (currentPlayer == null) {
            Log.e("BoardGameFragment", "Current player from ViewModel is null!")
        } else {
            Log.d("BoardGameFragment", "Current player from ViewModel is: ${currentPlayer.name}")
        }

        viewModel.startGame()


        // Toast feedback for game start
        rollDiceButton = view.findViewById(R.id.roll_dice_button)
        rollDiceButton.visibility = View.GONE

        rollDiceButton.setOnClickListener {
            showDiceDecisionDialog(viewModel.currentDiceResults.value ?: emptyList())
        }

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

        val playerInTokyoAvatar: ImageView = view.findViewById(R.id.player_in_tokyo_avatar)


        //Observe changes in the UI
        viewModel.uiEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is UIEvent.ShowDialog -> {
                    dialogQueue.offer(event)
                    if (dialogQueue.size == 1) {
                        displayNextDialog()
                    }
                }
            }
        }

        // Observe changes in the game state
        viewModel.gameState.observe(viewLifecycleOwner) { gameState ->

            val players = gameState.players
            if (players.isNotEmpty()) {
                player1Pseudo.text = players[0].name
                player1Life.text = players[0].health.toString() + " ❤️"
                player1Energy.text = players[0].energy.toString() + " ⚡"

                player2Pseudo.text = players[1].name
                player2Life.text = players[1].health.toString() + " ❤️"
                player2Energy.text = players[1].energy.toString() + " ⚡"

                player3Pseudo.text = players[2].name
                player3Life.text = players[2].health.toString() + " ❤️"
                player3Energy.text = players[2].energy.toString() + " ⚡"

                player4Pseudo.text = players[3].name
                player4Life.text = players[3].health.toString() + " ❤️"
                player4Energy.text = players[3].energy.toString() + " ⚡"

                // Highlight the current player
                val currentPlayer = gameState.currentTurnPlayer
                if (currentPlayer is Player && currentPlayer.isHuman) {
                    rollDiceButton.visibility = View.VISIBLE
                } else {
                    rollDiceButton.visibility = View.GONE
                }
                val secondaryColor = resources.getColor(R.color.secondary)
                player1Pseudo.setBackgroundColor(if (currentPlayer == players[0]) secondaryColor else Color.TRANSPARENT)
                player2Pseudo.setBackgroundColor(if (currentPlayer == players[1]) secondaryColor else Color.TRANSPARENT)
                player3Pseudo.setBackgroundColor(if (currentPlayer == players[2]) secondaryColor else Color.TRANSPARENT)
                player4Pseudo.setBackgroundColor(if (currentPlayer == players[3]) secondaryColor else Color.TRANSPARENT)

            }

            // Check if there's a player in Tokyo and update the UI
            val playerInTokyo = players.find { it.isInTokyo }
            if (playerInTokyo != null) {
                val playerInTokyoName: TextView = view.findViewById(R.id.player_in_tokyo_name)
                playerInTokyoName.text = playerInTokyo.name

                // If the player in Tokyo  if they want to leave
                if (playerInTokyo.hasBeenAttacked && playerInTokyo.isHuman) {
                    showLeaveTokyoDialog(playerInTokyo)
                }
            }

            //change image depending on user type
            if (playerInTokyo is IAPlayer) {
                playerInTokyoAvatar.setImageResource(R.drawable.ia_image)
            } else {
                playerInTokyoAvatar.setImageResource(R.drawable.default_image)
            }

        }

    }

    private fun showDiceDecisionDialog(diceResults: List<DiceFace>) {
        val diceToKeep = mutableListOf<DiceFace>()
        val diceArray = diceResults.toTypedArray()
        val checkedItems = BooleanArray(diceResults.size) { false }

        AlertDialog.Builder(requireContext())
            .setTitle("Select dice to keep")
            .setMultiChoiceItems(diceArray.map { it.name }.toTypedArray(), checkedItems) { _, which, isChecked ->
                if (isChecked) {
                    diceToKeep.add(diceArray[which])
                } else {
                    diceToKeep.remove(diceArray[which])
                }
            }
            .setPositiveButton("OK") { _, _ ->
                viewModel.rollDiceForCurrentPlayer(diceToKeep)
            }
            .show()
    }


    private fun showLeaveTokyoDialog(player: Player) {
        AlertDialog.Builder(requireContext())
            .setTitle("Leave Tokyo?")
            .setMessage("${player.name}, do you want to leave Tokyo?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.playerLeavesTokyo(player)
            }
            .setNegativeButton("No", null)
            .show()
    }
    private fun displayNextDialog() {
        if (dialogQueue.isNotEmpty()) {
            val event = dialogQueue.peek()
            showDialog(event.title, event.message, event.onDismiss)
        }
    }

    private fun showDialog(title: String, message: String, onDismiss: (() -> Unit)? = null) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                dialogQueue.poll()
                displayNextDialog()
                onDismiss?.invoke()
            }
            .show()
    }

}