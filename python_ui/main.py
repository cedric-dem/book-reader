import tkinter as tk
from dataclasses import dataclass

from config import (
	BACKGROUND_COLOR,
	COMMA_DELAY,
	DOT_DELAY,
	GENERAL_DELAY,
	OFFSET_DELAY_MS,
	SCREEN_SIZE,
	SPEED_PERCENTAGE,
	STEP_PERCENTAGE,
	WORD_SIZE_REFERENCE,
)

books = {
	"lotr": ['This', 'book', 'is', 'largely', 'concerned', 'with', 'Hobbits,', 'and', 'from', 'its', 'pages', 'a', 'reader', 'may', 'discover', 'much', 'of', 'their', 'character', 'and', 'a', 'little', 'of', 'their', 'history.', 'Further', 'information', 'will', 'also', 'be', 'found', 'in', 'the',
			 'selection', 'from', 'the', 'Red', 'Book', 'of', 'Westmarch', 'that', 'has', 'already', 'been', 'published,', 'under', 'the', 'title', 'of', 'The', 'Hobbit.', 'That', 'story', 'was', 'derived', 'from', 'the', 'earlier', 'chapters', 'of', 'the', 'Red', 'Book,', 'composed', 'by', 'Bilbo',
			 'himself,', 'the', 'first', 'Hobbit', 'to', 'become', 'famous', 'in', 'the', 'world', 'at', 'large,', 'and', 'called', 'by', 'him', 'There', 'and', 'Back', 'Again,', 'since', 'they', 'told', 'of', 'his', 'journey', 'into', 'the', 'East', 'and', 'his', 'return:', 'an', 'adventure',
			 'which', 'later', 'involved', 'all', 'the', 'Hobbits', 'in', 'the', 'great', 'events', 'of', 'that', 'Age', 'that', 'are', 'here', 'related.', 'Many,', 'however,', 'may', 'wish', 'to', 'know', 'more', 'about', 'this', 'remarkable', 'people', 'from', 'the', 'outset,', 'while', 'some',
			 'may', 'not', 'possess', 'the', 'earlier', 'book.', 'For', 'such', 'readers', 'a', 'few', 'notes', 'on', 'the', 'more', 'important', 'points', 'are', 'here', 'collected', 'from', 'Hobbit-lore,', 'and', 'the', 'first', 'adventure', 'is', 'briefly', 'recalled.', 'Hobbits', 'are', 'an',
			 'unobtrusive', 'but', 'very', 'ancient', 'people,', 'more', 'numerous', 'formerly', 'than', 'they', 'are', 'today;', 'for', 'they', 'love', 'peace', 'and', 'quiet', 'and', 'good', 'tilled', 'earth:', 'a', 'well-ordered', 'and', 'well-farmed', 'countryside', 'was', 'their', 'favourite',
			 'haunt.',
			 'They', 'do', 'not', 'and', 'did', 'not', 'understand', 'or', 'like', 'machines', 'more', 'complicated', 'than', 'a', 'forge-bellows,', 'a', 'water-mill,', 'or', 'a', 'hand-loom,', 'though', 'they', 'were', 'skilful', 'with', 'tools.', 'Even', 'in', 'ancient', 'days', 'they', 'were,',
			 'as', 'a', 'rule,', 'shy', 'of', '‘the', 'Big', 'Folk’,', 'as', 'they', 'call', 'us,', 'and', 'now', 'they', 'avoid', 'us', 'with', 'dismay', 'and', 'are', 'becoming', 'hard', 'to', 'find.', 'They', 'are', 'quick', 'of', 'hearing', 'and', 'sharp-eyed,', 'and', 'though', 'they', 'are',
			 'inclined', 'to', 'be', 'fat', 'and', 'do', 'not', 'hurry', 'unnecessarily,', 'they', 'are', 'nonetheless', 'nimble', 'and', 'deft', 'in', 'their', 'movements.', 'They', 'possessed', 'from', 'the', 'first', 'the', 'art', 'of', 'disappearing', 'swiftly', 'and', 'silently,', 'when',
			 'large', 'folk',
			 'whom', 'they', 'do', 'not', 'wish', 'to', 'meet', 'come', 'blundering', 'by;', 'and', 'this', 'art', 'they', 'have', 'developed', 'until', 'to', 'Men', 'it', 'may', 'seem', 'magical.', 'But', 'Hobbits', 'have', 'never,', 'in', 'fact,', 'studied', 'magic', 'of', 'any', 'kind,', 'and',
			 'their', 'elusiveness', 'is', 'due', 'solely', 'to', 'a', 'professional', 'skill', 'that', 'heredity', 'and', 'practice,', 'and', 'a', 'close', 'friendship', 'with', 'the', 'earth,', 'have', 'rendered', 'inimitable', 'by', 'bigger', 'and', 'clumsier', 'races.'],
	"test_1": ["this", "is", "the", "second", "book"],
	"test_2": ["this", "is", "the", "third", "book"]
}

@dataclass
class ReaderState:
	current_book: str = "lotr"
	current_word_index: int = 0
	current_view: str = "menu"
	word_update_after_id: str | None = None
	speed_percentage: int = SPEED_PERCENTAGE

class ReaderApp:
	def __init__(self, root: tk.Tk) -> None:
		self.root = root
		self.state = ReaderState()
		self.words = books[self.state.current_book]

		self.root.title("Word Reader")
		self.root.geometry(f"{SCREEN_SIZE[0]}x{SCREEN_SIZE[1]}")
		self.root.resizable(False, False)
		self.root.configure(bg = BACKGROUND_COLOR)

		self.current_word = tk.Label(root, text = "", font = ("Arial", 50), anchor = "center", justify = "center", bg = "black", fg = "white", )

		self.menu_title_label = tk.Label(root, text = "Menu", font = ("Arial", 32, "bold"), bg = BACKGROUND_COLOR, fg = "white", )

		self.menu_instructions_label = tk.Label(root, text = "Press 'j' for lotr, 'k' for test_1, 'l' for test_2\nPress '9' while reading to return here", font = ("Arial", 16), justify = "center", bg = BACKGROUND_COLOR, fg = "white", )

		self.page_progression_label = tk.Label(root, text = "This page progression :\n308 / 1099 words", font = ("Arial", 14), anchor = "w", justify = "left", bg = BACKGROUND_COLOR, fg = "white", )

		self.current_chapter_label = tk.Label(root, text = "Current chapter:\n1 / 12", font = ("Arial", 14), anchor = "center", justify = "center", bg = BACKGROUND_COLOR, fg = "white", )

		self.book_progression_label = tk.Label(root, text = "Book progression :\n12 / 500 pages ", font = ("Arial", 14), anchor = "e", justify = "right", bg = BACKGROUND_COLOR, fg = "white", )

		self.battery_label = tk.Label(root, text = "Battery :\n36 %", font = ("Arial", 14), anchor = "w", justify = "left", bg = BACKGROUND_COLOR, fg = "white", )

		self.speed_label = tk.Label(root, text = "Delay :\n100 %", font = ("Arial", 14), anchor = "e", justify = "right", bg = BACKGROUND_COLOR, fg = "white", )

		self.root.bind("<Key>", self.key_pressed)
		self.show_menu()

	def update_speed_label(self) -> None:
		self.speed_label.config(text = f"Delay : {self.state.speed_percentage:.0f}%")

	def calculate_delay_seconds(self, word: str) -> float:
		word_size_coefficient = len(word) / WORD_SIZE_REFERENCE

		if word.endswith("."):
			punctuation_coefficient = DOT_DELAY
		elif word.endswith(","):
			punctuation_coefficient = COMMA_DELAY
		else:
			punctuation_coefficient = 1

		return OFFSET_DELAY_MS + (
				punctuation_coefficient
				* word_size_coefficient
				* GENERAL_DELAY
				* (self.state.speed_percentage / 100)
		)

	def update_word(self) -> None:
		word = self.words[self.state.current_word_index]
		self.current_word.config(text = word)

		delay_seconds = self.calculate_delay_seconds(word)
		self.state.current_word_index = (self.state.current_word_index + 1) % len(self.words)

		self.state.word_update_after_id = self.root.after(
			int(delay_seconds * 1000), self.update_word
		)

	def cancel_word_update(self) -> None:
		if self.state.word_update_after_id is not None:
			self.root.after_cancel(self.state.word_update_after_id)
			self.state.word_update_after_id = None

	def show_menu(self) -> None:
		self.state.current_view = "menu"
		self.cancel_word_update()

		self.current_word.place_forget()
		self.current_chapter_label.place_forget()
		self.page_progression_label.place_forget()
		self.book_progression_label.place_forget()
		self.battery_label.place_forget()
		self.speed_label.place_forget()

		self.menu_title_label.place(
			x = int(0.24 * SCREEN_SIZE[0]),
			y = int(0.28 * SCREEN_SIZE[1]),
			width = int(0.52 * SCREEN_SIZE[0]),
			height = int(0.08 * SCREEN_SIZE[1]),
		)
		self.menu_instructions_label.place(
			x = int(0.12 * SCREEN_SIZE[0]),
			y = int(0.44 * SCREEN_SIZE[1]),
			width = int(0.76 * SCREEN_SIZE[0]),
			height = int(0.20 * SCREEN_SIZE[1]),
		)

	def show_reader(self) -> None:
		if self.state.current_view == "reader":
			return

		self.state.current_view = "reader"
		self.menu_title_label.place_forget()
		self.menu_instructions_label.place_forget()

		self.current_word.place(
			x = int(0 * SCREEN_SIZE[0]),
			y = int(0.30 * SCREEN_SIZE[1]),
			width = int(1.0 * SCREEN_SIZE[0]),
			height = int(0.40 * SCREEN_SIZE[1]),
		)
		self.page_progression_label.place(
			x = int(0.02 * SCREEN_SIZE[0]),
			y = int(0.02 * SCREEN_SIZE[1]),
			width = int(0.36 * SCREEN_SIZE[0]),
			height = int(0.09 * SCREEN_SIZE[1]),
		)
		self.current_chapter_label.place(
			x = int(0.30 * SCREEN_SIZE[0]),
			y = int(0.02 * SCREEN_SIZE[1]),
			width = int(0.40 * SCREEN_SIZE[0]),
			height = int(0.09 * SCREEN_SIZE[1]),
		)
		self.book_progression_label.place(
			x = int(0.62 * SCREEN_SIZE[0]),
			y = int(0.02 * SCREEN_SIZE[1]),
			width = int(0.36 * SCREEN_SIZE[0]),
			height = int(0.09 * SCREEN_SIZE[1]),
		)
		self.battery_label.place(
			x = int(0.02 * SCREEN_SIZE[0]),
			y = int(0.89 * SCREEN_SIZE[1]),
			width = int(0.36 * SCREEN_SIZE[0]),
			height = int(0.09 * SCREEN_SIZE[1]),
		)
		self.speed_label.place(
			x = int(0.62 * SCREEN_SIZE[0]),
			y = int(0.89 * SCREEN_SIZE[1]),
			width = int(0.36 * SCREEN_SIZE[0]),
			height = int(0.09 * SCREEN_SIZE[1]),
		)

		self.update_word()

	def select_book(self, book_name: str) -> None:
		if book_name not in books:
			print(f"Unknown book: {book_name}")
			return

		self.state.current_book = book_name
		self.words = books[book_name]
		self.state.current_word_index = 0
		self.show_reader()
		print(f"Opening book: {book_name}")

	def update_speed(self, increase: bool) -> None:
		if increase:
			self.state.speed_percentage += STEP_PERCENTAGE
			print(f"Delay increased : {self.state.speed_percentage:.2f}%")
			self.update_speed_label()
			return

		if self.state.speed_percentage > STEP_PERCENTAGE + 1:
			self.state.speed_percentage -= STEP_PERCENTAGE
			print(f"Delay decreased : {self.state.speed_percentage:.2f}%")
			self.update_speed_label()
		else:
			print("Min delay reached")

	def key_pressed(self, event: tk.Event) -> None:
		key = event.char.lower()

		if self.state.current_view == "menu":
			if key == "j":
				self.select_book("lotr")
			elif key == "k":
				self.select_book("test_1")
			elif key == "l":
				self.select_book("test_2")
			return

		if key == "1":
			self.update_speed(increase = False)
		elif key == "2":
			self.update_speed(increase = True)
		elif key == "3":
			print("Previous page")
		elif key == "4":
			print("Next page")
		elif key == "5":
			print("Go back 10 words")
		elif key == "6":
			print("Jump 10 words")
		elif key == "7":
			print("Pause")
		elif key == "8":
			print("Switch View")
		elif key == "9":
			self.show_menu()
			print("Home")

def main() -> None:
	root = tk.Tk()
	ReaderApp(root)
	root.mainloop()

if __name__ == "__main__":
	main()
