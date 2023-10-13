//BoardGameFragment

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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.kingoftokyo.Entity.Card
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
    private lateinit var rollDiceAgainButton: Button // Bouton pour lancer les dés à nouveau

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_board_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.startGame()


        // Toast feedback for game start
        rollDiceButton = view.findViewById(R.id.roll_dice_button)
        rollDiceButton.visibility = View.GONE

        rollDiceButton.setOnClickListener {
            Log.d("BoardGameFragment", "Current dice results: ${viewModel.currentDiceResults.value}")
            showDiceDecisionDialog(viewModel.currentDiceResults.value ?: emptyList())
        }

        // Bouton pour lancer les dés à nouveau
        rollDiceAgainButton = view.findViewById(R.id.roll_dice_again_button)
        rollDiceAgainButton.visibility = View.GONE
        rollDiceAgainButton.setOnClickListener {
            viewModel.rollDiceAgainForHumanPlayer()
        }

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
            val currentPlayer = viewModel.gameState.value?.currentTurnPlayer
            if (currentPlayer == null) {
                Log.e("BoardGameFragment", "Current player from ViewModel is null!")
            } else {
                Log.d("BoardGameFragment", "Current player from ViewModel is: ${currentPlayer.name}")
            }

            val players = gameState.players
            if (players.isNotEmpty()) {
                for(i in 1..4) {
                    val player_pseudo_id = resources.getIdentifier("player_${i}_pseudo", "id", requireContext().packageName);
                    val player_life_id = resources.getIdentifier("player_${i}_life", "id", requireContext().packageName)
                    val player_energy_id = resources.getIdentifier("player_${i}_energy", "id", requireContext().packageName)
                    val player_cards_id = resources.getIdentifier("player_${i}_cards", "id", requireContext().packageName);
                    val player_pseudo = view.findViewById<TextView>(player_pseudo_id);
                    val player_life = view.findViewById<TextView>(player_life_id);
                    val player_energy = view.findViewById<TextView>(player_energy_id);
                    val player_cards = view.findViewById<LinearLayout>(player_cards_id)

                    player_pseudo.text = players[i - 1].name;
                    player_life.text = "${players[i - 1].health} ❤️";
                    player_energy.text = "${players[i - 1].energy} ⚡";

                    player_cards.removeAllViews();

                    for(card in players[i - 1].getCards()) {
                        player_cards.addView(generateCard(card));
                    }
                }

                // Highlight the current player
                val currentPlayer = gameState.currentTurnPlayer
                if (currentPlayer is Player && currentPlayer.isHuman) {
                    rollDiceButton.visibility = View.VISIBLE
                } else {
                    rollDiceButton.visibility = View.GONE
                }
                val secondaryColor = resources.getColor(R.color.secondary)

                for(i in 1..4) {
                    val playerPseudo = view.findViewById<TextView>(resources.getIdentifier("player_${i}_pseudo", "id", requireContext().packageName));
                    playerPseudo.setBackgroundColor(if(currentPlayer == players[i - 1]) secondaryColor else Color.TRANSPARENT);
                }

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

        Log.d("BoardGameFragment", "Dice to show in dialog: $diceResults")

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
                rollDiceAgainButton.visibility = View.VISIBLE // Afficher le bouton de lancer à nouveau
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




    private fun generateCard(card : Card) : View {
       val cardText = TextView(this.context);
        val layoutParameters = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardText.text = "🃏 ${card.name}";
        cardText.layoutParams = layoutParameters;
        cardText.setTextColor(Color.WHITE);
        cardText.textSize = 16.0F
        return cardText;
    }

}

