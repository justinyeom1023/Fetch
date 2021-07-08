package com.example.fetch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.json.JSONException
import org.json.JSONObject
import android.os.AsyncTask
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection
import java.util.*
import kotlin.Comparator
import org.json.JSONArray
import android.content.Context
import android.text.Layout
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        FetchTask().execute()
    }


//    class CustomAdapter(private val dataSet: ArrayList<String>) :
//        RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
//
//        init {
//            setHasStableIds(true)
//        }
//
//        /**
//         * Provide a reference to the type of views that you are using
//         * (custom ViewHolder).
//         */
//        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//            val textView: TextView
//
//            init {
//                // Define click listener for the ViewHolder's View.
//                textView = view.findViewById(R.id.textList)
//            }
//        }
//
//        // Create new views (invoked by the layout manager)
//        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
//
//            // Create a new view, which defines the UI of the list item
//            val view = LayoutInflater.from(viewGroup.context)
//                .inflate(R.layout.activity_listview, viewGroup, false)
//
//
//
//            return ViewHolder(view)
//        }
//
//        override fun getItemId(position: Int): Long {
//            return position.toLong()
//        }
//
//        override fun getItemViewType(position: Int): Int {
//            return position
//        }
//
//        // Replace the contents of a view (invoked by the layout manager)
//        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
//
//
//            // Get element from your dataset at this position and replace the
//            // contents of the view with that element
//            viewHolder.textView.text = dataSet[position]
//        }
//
//        // Return the size of your dataset (invoked by the layout manager)
//        override fun getItemCount() = dataSet.size
//
//    }

    class FetchAdapter(private val context: Context,
                        private val dataSource: ArrayList<String>) : BaseAdapter() {

        private val inflater: LayoutInflater
                = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getCount(): Int {
            return dataSource.size
        }

        override fun getItem(position: Int): Any {
            return dataSource[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View
            val holder: ViewHolder

            // 1
            if (convertView == null) {

                view = inflater.inflate(R.layout.activity_listview, parent, false)

                holder = ViewHolder()
                holder.titleTextView = view.findViewById(R.id.textList) as TextView
                view.tag = holder
            } else {
                view = convertView
                holder = convertView.tag as ViewHolder
            }

            val titleTextView = holder.titleTextView

            val string = getItem(position) as String

            titleTextView.text = string

            return view
        }

        private class ViewHolder {
            lateinit var titleTextView: TextView
        }
    }

    protected inner class FetchTask : AsyncTask<Void, Void, JSONArray>() {
        override fun doInBackground(vararg params: Void): JSONArray? {

            val str = "https://fetch-hiring.s3.amazonaws.com/hiring.json"
            var urlConn: URLConnection? = null
            var bufferedReader: BufferedReader? = null
            try {
                val url = URL(str)
                urlConn = url.openConnection()
                bufferedReader = BufferedReader(InputStreamReader(urlConn!!.getInputStream()))

                val stringBuffer = StringBuffer()
                var line: String? = bufferedReader!!.readLine()
                while (line != null) {
                    stringBuffer.append(line)
                    line = bufferedReader!!.readLine()
                }

                Log.d("DoSomething", "" + stringBuffer.toString().length + "" )

                return JSONArray(stringBuffer.toString())
            } catch (ex: Exception) {
                Log.e("App", "Fetch", ex)
                return null
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader!!.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }

        override fun onPostExecute(response: JSONArray?) {
            if (response != null) {
                try {
                    Log.e("App", "Success: " + response.getString(0))
                } catch (ex: JSONException) {
                    Log.e("App", "Failure", ex)
                }

                val jsonValues = ArrayList<JSONObject>()
                for (i in 0 until response.length()) {
                    val jsonObject = response.getJSONObject(i)

                    if (jsonObject.get("name").toString().length > 0 && !jsonObject.get("name").toString().equals("null")) {
                        jsonValues.add(response.getJSONObject(i))
                    }

                }
                Collections.sort(jsonValues, object : Comparator<JSONObject> {

                    override fun compare(a: JSONObject, b: JSONObject): Int {
                        var valA = 0
                        var valB = 0

                        try {
                            valA = a.get("listId") as Int
                            valB = b.get("listId") as Int
                        } catch (e: JSONException) {

                        }

                        val compareVal = valA.compareTo(valB)

                        if (compareVal != 0) {
                            return compareVal
                        }

                        var valC = String()
                        var valD = String()

                        try {
                            valC = a.get("name") as String
                            valD = b.get("name") as String
                        } catch (e: JSONException) {

                        }

                        return valA.compareTo(valB)

                    }
                })

                Log.i("DoSomething",""+jsonValues.size)

                val displayList = ArrayList<String>()

                for (i in 0 until jsonValues.size) {
                    displayList.add(jsonValues.get(i).toString(1))
                }

                runOnUiThread(


                    object : Runnable {
                        override fun run() {

                            val adapter = FetchAdapter(
                                this@MainActivity,displayList
                            )

//                            val adapter = CustomAdapter(displayList)

                            val recyclerView = findViewById(R.id.fetch_list) as ListView

//                            recyclerView.setLayoutManager(LinearLayoutManager(this@MainActivity));
                            recyclerView.setAdapter(adapter);

                            Log.i("App", "runOnUiThread")
                        }
                    }
                )
            }
        }
    }
}
