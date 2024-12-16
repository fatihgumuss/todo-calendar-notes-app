package com.example.thenotesapp.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.thenotesapp.MainActivity
import com.example.thenotesapp.R
import com.example.thenotesapp.databinding.FragmentEditNoteBinding
import com.example.thenotesapp.model.Note
import com.example.thenotesapp.service.GeminiService
import com.example.thenotesapp.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

class EditNoteFragment : Fragment(R.layout.fragment_edit_note), MenuProvider {

    private var editNoteBinding: FragmentEditNoteBinding? = null
    private val binding get() = editNoteBinding!!

    private lateinit var notesViewModel: NoteViewModel
    private lateinit var currentNote: Note
    private var selectedImageUri: Uri? = null


    private val args: EditNoteFragmentArgs by navArgs()

    private lateinit var geminiService: GeminiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        editNoteBinding = FragmentEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        notesViewModel = (activity as MainActivity).noteViewModel
        currentNote = args.note!!

        binding.editImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            resultLauncher.launch(intent)
        }
        if (currentNote.imagePath != null) {
            selectedImageUri = Uri.parse(currentNote.imagePath) // Resmi yükle
            binding.editNoteImageView.setImageURI(selectedImageUri)
            binding.editNoteImageView.visibility = View.VISIBLE
        }
        binding.editNoteTitle.setText(currentNote.noteTitle)
        binding.editNoteDesc.setText(currentNote.noteDesc)
        binding.backButton.setOnClickListener {
            view.findNavController().popBackStack()
        }

        geminiService = GeminiService(requireContext())

        binding.saveButton.setOnClickListener {
            val noteTitle = binding.editNoteTitle.text.toString().trim()
            val noteDesc = binding.editNoteDesc.text.toString().trim()

            if (noteTitle.isNotEmpty()) {
                val note = Note(currentNote.id, noteTitle, noteDesc, selectedImageUri.toString()) // Resmi nota kaydet
                notesViewModel.updateNote(note)
                view.findNavController().popBackStack(R.id.homeFragment, false)
            } else {
                Toast.makeText(context, "Lütfen not başlığını girin", Toast.LENGTH_SHORT).show()
            }
        }

        binding.deleteButton.setOnClickListener {
            deleteNote()
        }
        binding.optionsButton.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.inflate(R.menu.popup_menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.shareMenu -> {
                        val noteContent = "${binding.editNoteTitle.text}\n${binding.editNoteDesc.text}"
                        shareNote(requireContext(), noteContent)
                        true
                    }
                    R.id.summarizeMenu -> {
                        summarizeNote()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            binding.editNoteImageView.setImageURI(selectedImageUri)
            binding.editNoteImageView.visibility = View.VISIBLE
        }
    }
    private fun shareNote(context: Context, noteContent: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, noteContent)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    private fun deleteNote(){
        AlertDialog.Builder(activity).apply {
            setTitle("Delete Note")
            setMessage("Do you want to delete this note?")
            setPositiveButton("Delete"){_,_ ->
                notesViewModel.deleteNote(currentNote)
                Toast.makeText(context, " Note Deleted", Toast.LENGTH_SHORT).show()
                view?.findNavController()?.popBackStack(R.id.homeFragment, false)
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.menu_edit_note, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.deleteMenu -> {
                deleteNote()
                true
            } else -> false
        }
    }

    private fun summarizeNote() {
        val currentText = binding.editNoteDesc.text.toString()
        if (currentText.isBlank()) {
            Toast.makeText(context, "Note is empty", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val summary = geminiService.summarizeText(currentText)

                if (summary.startsWith("Error")) {
                    Toast.makeText(context, summary, Toast.LENGTH_LONG).show()
                    return@launch
                }

                // Append summary to existing text
                val updatedText = """
                    $currentText
                    
                    Summary:
                    $summary
                """.trimIndent()

                binding.editNoteDesc.setText(updatedText)

            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        editNoteBinding = null
    }

}