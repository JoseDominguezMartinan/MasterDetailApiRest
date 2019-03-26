package com.example.masterdetailapirest

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.example.masterdetailapirest.dummy.DummyContent
import kotlinx.android.synthetic.main.activity_item_list.*
import kotlinx.android.synthetic.main.item_list_content.view.*
import kotlinx.android.synthetic.main.item_list.*

import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast
import java.net.URL
import org.json.JSONArray
import org.jetbrains.anko.uiThread

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [ItemDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class ItemListActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var twoPane: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        fab.setOnClickListener { view ->
            Snackbar.make(view, "pulsado boton", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

        }

        if (item_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }
        wpPetition()

    }
    private fun wpPetition() {
        Posts.lista.clear()
        doAsync {

            // capturamos los errores de la peticion
            try {
                // peticion a un servidor rest que devuelve un json generico

                val respuesta = URL("http://52.14.208.12/wordpress/?rest_route=/wp/v2/posts").readText()

                // parsing data

                // sabemos que recibimos un array de objetos JSON
                uiThread {
                val miJSONArray = JSONArray(respuesta)

                // recorremos el Array

                for (jsonIndex in 0..(miJSONArray.length() - 1)) {

                    // creamos el objeto 'misDatos' a partir de la clase 'Datos'

                    // asignamos el valor de 'title' en el constructor de la data class 'Datos'
                    var titulo = miJSONArray.getJSONObject(jsonIndex).getJSONObject("title").getString("rendered")
                    var descripcion = miJSONArray.getJSONObject(jsonIndex).getJSONObject("excerpt").getString("rendered")
                    // salida procesada en Logcat
                    var post=Posts.Post(titulo,descripcion)
                    Posts.lista.add(post)
                }


                    // Log.d(LOGTAG, respuesta)
                    longToast("Request performed")
                }

                // Si algo va mal lo capturamos
            } catch (e: Exception) {
                uiThread {
                    longToast("Something go wrong: $e")
                }
            }finally {

                setupRecyclerView(item_list)
            }
            }

    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, Posts.lista, twoPane)
    }

    class SimpleItemRecyclerViewAdapter(
        private val parentActivity: ItemListActivity,
        private val values: MutableList<Posts.Post>,
        private val twoPane: Boolean
    ) :
        RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as Posts.Post
                if (twoPane) {
                    val fragment = ItemDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString(ItemDetailFragment.ARG_ITEM_ID, item.titulo)
                            putString(ItemDetailFragment.ARG_ITEM_DESC, item.descripcion)
                        }
                    }
                    parentActivity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit()
                } else {
                    val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                        putExtra(ItemDetailFragment.ARG_ITEM_ID, item.titulo)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.idView.text = item.titulo
            holder.contentView.text = item.descripcion

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.id_text
            val contentView: TextView = view.content
        }


    }
}
