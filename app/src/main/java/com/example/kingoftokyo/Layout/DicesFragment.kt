package com.example.kingoftokyo.Layout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.kingoftokyo.Entity.DiceFace
import com.example.kingoftokyo.R
import com.example.kingoftokyo.ViewModel.GameViewModel

class DiceFragment : Fragment() {

    private val viewModel: GameViewModel by viewModels()
    private lateinit var diceResultsListView: ListView
    private lateinit var rollDiceButton: Button
    private lateinit var finalizeTurnButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        diceResultsListView = view.findViewById(R.id.diceResultsListView)
        rollDiceButton = view.findViewById(R.id.rollDiceButton)
        finalizeTurnButton = view.findViewById(R.id.finalizeTurnButton)

        // Set up the dice results list view
        val diceResults = viewModel.currentDiceResults.value ?: emptyList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_multiple_choice, diceResults)
        diceResultsListView.adapter = adapter
        diceResultsListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE

        // Set up the roll dice button
        rollDiceButton.setOnClickListener {
            // Roll the dice for the current player, keeping the selected dice
            val selectedDice = mutableListOf<DiceFace>()
            for (i in 0 until diceResultsListView.checkedItemPositions.size()) {
                if (diceResultsListView.checkedItemPositions.valueAt(i)) {
                    selectedDice.add(diceResults[diceResultsListView.checkedItemPositions.keyAt(i)])
                }
            }

            viewModel.rollDiceForCurrentPlayer(selectedDice)

            // Update the dice results list view with the new dice results
            val newDiceResults = viewModel.currentDiceResults.value ?: emptyList()
            val newAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_multiple_choice, newDiceResults)
            diceResultsListView.adapter = newAdapter
        }

        // Set up the finalize turn button
        finalizeTurnButton.setOnClickListener {
            // Finalize the turn for the current player
            viewModel.nextTurn()

            // Navigate back to the main game screen (e.g., BoardGameFragment)
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, BoardGameFragment())
                .commit()
        }
    }
}
