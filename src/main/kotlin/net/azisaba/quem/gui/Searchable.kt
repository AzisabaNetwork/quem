package net.azisaba.quem.gui

import com.tksimeji.visualkit.IVisualkitUI

interface Searchable: IVisualkitUI {
    fun search(query: String)
}