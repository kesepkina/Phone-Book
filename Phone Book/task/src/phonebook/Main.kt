package phonebook

import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.HashSet
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class Contact(val index: Long, val name: String)

fun main() {
    val file =
        File("C:\\Users\\KseniyaEsepk_1exxjn3\\IdeaProjects\\Phone Book\\Phone Book\\task\\src\\phonebook\\directory.txt")
    val allContacts = file.readLines().map { item -> item.split(" ", limit = 2) }
        .map { Contact(it[0].toLong(), it[1]) }.toMutableList()
    val namesToFind =
        File("C:\\Users\\KseniyaEsepk_1exxjn3\\IdeaProjects\\Phone Book\\Phone Book\\task\\src\\phonebook\\find.txt")
            .readLines().toMutableList()
    val startSize = namesToFind.size

    println("Start searching (linear search)...")
    val (timeTakenLinearSearch, count) = performLinearSearch(allContacts, namesToFind)
    outputSearchResults(startSize, count, timeTakenLinearSearch)

    println("\nStart searching (bubble sort + jump search)...")
    val (sortedContacts, timeTakenForSorting, stopped) = performBubbleSort(
        allContacts, timeTakenLinearSearch.multipliedBy(
            10L
        )
    )
    val (timeTakenForSecondSearch, contactsFoundNum) = if (stopped) {
        performLinearSearch(sortedContacts, namesToFind)
    } else {
        performJumpSearch(sortedContacts, namesToFind)
    }
    outputSearchResults(
        startSize,
        contactsFoundNum,
        timeTakenForSecondSearch + timeTakenForSorting,
        timeTakenForSorting,
        timeTakenForSecondSearch,
        stopped
    )

    println("\nStart searching (quick sort + binary search)...")
    val (quickSortedContacts, timeTakenForQuickSorting) = performQuickSort(allContacts)
    val (timeTakenBinarySearch, numFound) = performBinarySearch(quickSortedContacts, namesToFind)
    outputSearchResults(
        startSize,
        numFound,
        timeTakenForQuickSorting + timeTakenBinarySearch,
        timeTakenForQuickSorting,
        timeTakenBinarySearch
    )

    println("\nStart searching (hash table)...")
    val start = Instant.now()
    val hashedContactsNames = allContacts.map { it.name }.toHashSet()
    val tameTakenForCreatingHashSet = Duration.between(start, Instant.now())
    val (timeTakenHashSearch, numFoundInHashSet) = performSearchInHashSet(hashedContactsNames, namesToFind)
    outputSearchResults(
        startSize,
        numFoundInHashSet,
        tameTakenForCreatingHashSet + timeTakenHashSearch,
        creatingTime = tameTakenForCreatingHashSet,
        searchingTime = timeTakenHashSearch
    )
}

fun performSearchInHashSet(hashSet: HashSet<String>, namesToFind: MutableList<String>): Pair<Duration, Int> {
    val start = Instant.now()
    var count = 0
    for (name in namesToFind) {
        if (hashSet.contains(name)) count++
    }
    return Pair(Duration.between(start, Instant.now()), count)
}

fun getHash(value: String, numOfBuckets: Int): Long {
    var hash = ""
    for (char in value) {
        hash += char.code
    }
    return hash.toLong() % numOfBuckets
}

fun performBinarySearch(contacts: MutableList<Contact>, namesToFind: MutableList<String>): Pair<Duration, Int> {
    var count = 0
    val start = Instant.now()
    names@ for (name in namesToFind) {
        var left = 0
        var right = contacts.lastIndex
        while (left <= right) {
            val middle = (left + right) / 2
            val middleName = contacts[middle].name
            if (middleName == name) {
                count++
                continue@names
            } else if (middleName > name) {
                right = middle - 1
            } else {
                left = middle + 1
            }
        }
        println(name)
    }
    return Pair(Duration.between(start, Instant.now()), count)
}

fun performQuickSort(contacts: MutableList<Contact>): Pair<MutableList<Contact>, Duration> {
    val start = Instant.now()
    val contactsCopy = contacts.toMutableList()
    rearrangeRelativeToPivot(contactsCopy, 0, contacts.lastIndex)
    return Pair(contactsCopy, Duration.between(start, Instant.now()))
}

fun rearrangeRelativeToPivot(contacts: MutableList<Contact>, left: Int, right: Int) {
    if (left >= right) return
    var pivot = findMedianContactIndex(contacts, left, right)
    var i = left
    var j = right
    val pivotName = contacts[pivot].name
    while (pivot in (i + 1) until j) {
        if (contacts[i].name <= pivotName) i++
        if (contacts[j].name >= pivotName) j--
        if (contacts[i].name > pivotName && contacts[j].name < pivotName) {
            contacts[i] = contacts[j].also { contacts[j] = contacts[i] }
            i++
            j--
        }
    }
    if (i >= pivot) {
        while (j > pivot) {
            if (contacts[j].name < pivotName) {
                contacts[j] = contacts[pivot + 1].also { contacts[pivot + 1] = contacts[j] }
                contacts[pivot] = contacts[pivot + 1].also { contacts[pivot + 1] = contacts[pivot] }
                pivot++
            } else j--
        }
    } else if (j <= pivot) {
        while (i < pivot) {
            if (contacts[i].name > pivotName) {
                contacts[i] = contacts[pivot - 1].also { contacts[pivot - 1] = contacts[i] }
                contacts[pivot] = contacts[pivot - 1].also { contacts[pivot - 1] = contacts[pivot] }
                pivot--
            } else i++
        }
    }
    rearrangeRelativeToPivot(contacts, left, pivot - 1)
    rearrangeRelativeToPivot(contacts, pivot + 1, right)
}

fun findMedianContactIndex(contacts: MutableList<Contact>, left: Int, right: Int): Int {
    val middle = (left + right) / 2
    val min = min(min(contacts[left].name, contacts[right].name), contacts[middle].name)
    val max = max(max(contacts[left].name, contacts[right].name), contacts[middle].name)
    return when {
        contacts[left].name in listOf(min, max) && contacts[right].name in listOf(min, max) -> middle
        contacts[left].name in listOf(min, max) && contacts[middle].name in listOf(min, max) -> right
        else -> left
    }
}

fun min(a: String, b: String): String = if (a <= b) a else b
fun max(a: String, b: String): String = if (a >= b) a else b

fun performJumpSearch(contacts: MutableList<Contact>, namesToFind: MutableList<String>): Pair<Duration, Int> {
    val size = contacts.size
    val step = floor(sqrt(size.toDouble())).toInt()
    var count = 0
    val start = Instant.now()
    names@ for (name in namesToFind) {
        var cur = 0
        var prev = 0
        while (contacts[cur].name < name) {
            if (cur == contacts.lastIndex) continue@names
            prev = cur
            cur = min(cur + step, contacts.lastIndex)
        }
        while (contacts[cur].name > name) {
            if (cur == prev) continue@names
            cur = max(cur - 1, prev)
        }
        if (contacts[cur].name == name) count++
    }
    return Pair(Duration.between(start, Instant.now()), count)
}

fun performBubbleSort(
    list: MutableList<Contact>,
    maxDuration: Duration
): Triple<MutableList<Contact>, Duration, Boolean> {
    val start = Instant.now()
    var stopped = false
    loop@ for (i in 0 until list.lastIndex) {
        for (j in i until list.lastIndex) {
            if (list[j].name > list[j + 1].name) {
                list[j] = list[j + 1].also { list[j + 1] = list[j] }
            }
            if (Duration.between(start, Instant.now()) > maxDuration) {
                stopped = true
                break@loop
            }
        }
    }
    return Triple(list, Duration.between(start, Instant.now()), stopped)
}

fun outputSearchResults(
    numToFind: Int,
    numFound: Int,
    timeTaken: Duration,
    sortingTime: Duration? = null,
    searchingTime: Duration? = null,
    sortingWasStopped: Boolean = false,
    creatingTime: Duration? = null
) {
    println(
        "Found $numFound / $numToFind entries. Time taken: ${timeTaken.toMinutes()} min. " +
                "${timeTaken.seconds % 60} sec. ${timeTaken.toMillis() % 1000} ms."
    )
    if (creatingTime != null) println(
        "Creating time: ${creatingTime.toMinutes()} min. ${creatingTime.seconds % 60} sec. " +
                "${creatingTime.toMillis() % 1000} ms."
    )
    if (sortingTime != null) println(
        "Sorting time: ${sortingTime.toMinutes()} min. ${sortingTime.seconds % 60} sec. " +
                "${sortingTime.toMillis() % 1000} ms.${if (sortingWasStopped) " STOPPED, moved to linear search" else ""}"
    )
    if (searchingTime != null) println(
        "Searching time: ${searchingTime.toMinutes()} min. ${searchingTime.seconds % 60} sec. " +
                "${searchingTime.toMillis() % 1000} ms."
    )
}

fun performLinearSearch(allContacts: List<Contact>, namesToFind: MutableList<String>): Pair<Duration, Int> {
    var count = 0
    val start = Instant.now()
    for (name in namesToFind) {
        for (contact in allContacts) {
            if (contact.name == name) {
                count++
                break
            }
        }
    }
    val end = Instant.now()
    val timeTaken = Duration.between(start, end)
    return Pair(timeTaken, count)
}
