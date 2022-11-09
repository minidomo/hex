package edu.utap.hex

import edu.utap.hex.model.ChatRow

class HexStrategy {
    enum class MoveType(val value: Int) {
        Random(0),
        ;
        companion object {
            private val map = values().associateBy(MoveType::value)
            operator fun get(value: Int) = map[value]
        }
    }
    companion object {
        private fun randomMove(viewModel: MainViewModel, hexGame: HexGame)
                : Pair<Int, Int> {
            while (true) {
                val col = viewModel.random().nextInt(1, hexGame.getBoardDim() - 1)
                val row = viewModel.random().nextInt(1, hexGame.getBoardDim() - 1)
                if (hexGame.legalMove(col, row)) {
                    return Pair(col, row)
                }
            }
        }

        private fun pickBestMove(viewModel: MainViewModel, hexGame: HexGame) : Pair<Int, Int> {
            when (MoveType[viewModel.random().nextInt(MoveType.values().size)]) {
                MoveType.Random -> return randomMove(viewModel, hexGame)
                else -> {}
            }
            return Pair(0, 0)
        }

        private val aiMessages = listOf(
            "Nice move",
            "Where did you study Hex?",
            "My transistors are itchy",
            "You need something to open up a new door, to show you something you seen before but overlooked a hundred times or more",
            "You’re going to die. You’re going to be dead. It could be 20 years, it could be tomorrow, anytime. So am I. I mean, we’re just going to be gone. The world’s going to go on without us. All right now. You do your job in the face of that, and how seriously you take yourself you decide for yourself."
        )

        private fun aiChat(viewModel: MainViewModel): String {
            if (viewModel.random().nextInt(8) == 0) {
                val msgIndex = viewModel.random().nextInt(aiMessages.size)
                return aiMessages[msgIndex]
            }
            return ""
        }

        fun aiMove(viewModel: MainViewModel, hexGame: HexGame) {
            val (col, row) = pickBestMove(viewModel, hexGame)
            assert(hexGame.legalMove(col, row))
            val aiMessage = aiChat(viewModel)
            if (aiMessage.isNotEmpty()) {
                val aiPlayer = HexPlayer.aiPlayer()
                val chatRow = ChatRow().apply {
                    name = aiPlayer.name
                    ownerUid = aiPlayer.uid
                    message = aiMessage
                    moveNumber = hexGame.moveNumber()
                }
                FirestoreDB.saveChatRow(chatRow)
            }
            hexGame.makeMove(col, row)
        }
    }
}