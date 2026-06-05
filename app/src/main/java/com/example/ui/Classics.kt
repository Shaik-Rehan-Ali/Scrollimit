package com.example.ui

data class ClassicBook(
    val title: String,
    val author: String,
    val summary: String,
    val text: String
)

object Classics {
    val list = listOf(
        ClassicBook(
            title = "Alice's Adventures in Wonderland",
            author = "Lewis Carroll",
            summary = "A classic fantasy story of discovery and curious nonsense.",
            text = """
                CHAPTER I. Down the Rabit-Hole

                Alice was beginning to get very tired of sitting by her sister on the bank, and of having nothing to do: once or twice she had peeped into the book her sister was reading, but it had no pictures or conversations in it, 'and what is the use of a book,' thought Alice 'without pictures or conversations?'

                So she was considering in her own mind (as well as she could, for the hot day made her feel very sleepy and stupid), whether the pleasure of making a daisy-chain would be worth the trouble of getting up and picking the daisies, when suddenly a White Rabbit with pink eyes ran close by her.

                There was nothing so VERY remarkable in that; nor did Alice think it so VERY much out of the way to hear the Rabbit say to itself, 'Oh dear! Oh dear! I shall be late!' (when she thought it over afterwards, it occurred to her that she ought to have wondered at this, but at the time it all seemed quite natural); but when the Rabbit actually TOOK A WATCH OUT OF ITS WAISTCOAT-POCKET, and looked at it, and then hurried on, Alice started to her feet, for it flashed across her mind that she had never before seen a rabbit with either a waistcoat-pocket, or a watch to take out of it, and burning with curiosity, she ran across the field after it, and fortunately was just in time to see it pop down a large rabbit-hole under the hedge.
                
                In another moment down went Alice after it, never once considering how in the world she was to get out again.
                
                The rabbit-hole went straight on like a tunnel for some way, and then dipped suddenly down, so suddenly that Alice had not a moment to think about stopping herself before she found herself falling down a very deep well.
                
                Either the well was very deep, or she fell very slowly, for she had plenty of time as she went down to look about her and to wonder what was going to happen next. First, she tried to look down and make out what she was coming to, but it was too dark to see anything; then she looked at the sides of the well, and noticed that they were filled with cupboards and book-shelves; here and there she saw maps and pictures hung upon pegs. She took down a jar from one of the shelves as she passed; it was labelled 'ORANGE MARMALADE', but to her great disappointment it was empty: she did not like to drop the jar for fear of killing somebody, so managed to put it into one of the cupboards as she fell past it.
            """.trimIndent()
        ),
        ClassicBook(
            title = "The Art of War",
            author = "Sun Tzu",
            summary = "Ancient wisdom on strategic planning, focus, and overcoming friction.",
            text = """
                CHAPTER I. Laying Plans
                
                1. Sun Tzu said: The art of war is of vital importance to the State.
                2. It is a matter of life and death, a road either to safety or to ruin. Hence it is a subject of inquiry which can on no account be neglected.
                3. The art of war, then, is governed by five constant factors, to be taken into account in one's deliberations, when seeking to determine the conditions obtaining in the field.
                4. These are: (1) The Moral Law; (2) Heaven; (3) Earth; (4) The Commander; (5) Method and discipline.
                
                CHAPTER II. Waging War
                
                1. Sun Tzu said: In the operations of war, where there are in the field a thousand swift chariots, as many heavy chariots, and a hundred thousand mail-clad soldiers, with provisions enough to carry them a thousand li, the expenditure at home and at the front, including entertainment of guests, small items such as glue and paint, and sums spent on chariots and armor, will reach the total of a thousand ounces of silver per day. Such is the cost of raising an army of 100,000 men.
                2. When you engage in actual fighting, if victory is long in coming, then men's weapons will grow dull and their ardor will be damped. If you lay siege to a town, you will exhaust your strength.
                3. Again, if the campaign is protracted, the resources of the State will not be equal to the strain.
                4. Now, when your weapons are dulled, your ardor damped, your strength exhausted and your treasure spent, other chieftains will spring up to take advantage of your extremity. Then no man, however wise, will be able to avert the consequences that must ensue.
            """.trimIndent()
        ),
        ClassicBook(
            title = "The Meditations",
            author = "Marcus Aurelius",
            summary = "Timeless stoic principles on self-mastery, focus, and resisting trivial lures.",
            text = """
                BOOK ONE. Principles of Self-Regulation

                1. From my grandfather Verus I learned good morals and the government of my temper.
                2. From the reputation and memory of my father, modesty and a manly character.
                3. From my mother, piety and beneficence, and abstinence, not only from evil deeds, but even from evil thoughts; and further, simplicity in my way of living, far removed from the habits of the rich.
                4. From my great-grandfather, not to have frequented public schools, and to have had good teachers at home, and to know that on such things a man should spend liberally.

                5. From my tutor, not to be a partisan of the Green or the Blue faction at the races, nor of the Light-armed or Heavy-armed gladiators; and also to endure labor, and to want little, and to work with my own hands, and not to meddle with other people's affairs, and not to be ready to listen to slander.

                6. From Diognetus, not to busy myself about trifling things, and not to give credit to what was said by writers of miracles and stories of dreams, and to learn to bear freedom of speech; and to have become intimate with philosophy, and to have been a hearer in turn of Bacchius, then of Tandasis and Marcianus; and to have written dialogues in my youth; and to have desired a plank bed and skin, and whatever else of the kind belongs to the Grecian discipline.
            """.trimIndent()
        )
    )
}
