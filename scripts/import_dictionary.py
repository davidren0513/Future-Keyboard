"""Compatibility entrypoint: rebuild the pinned indexed dictionary."""
import runpy
from pathlib import Path
runpy.run_path(str(Path(__file__).with_name("compile_dictionary.py")),run_name="__main__")
