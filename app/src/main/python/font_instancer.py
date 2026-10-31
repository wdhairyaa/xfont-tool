import json
import logging
from fontTools.ttLib import TTFont
from fontTools.varLib import instancer

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("FontInstancer")

def freeze_font(input_path: str, output_path: str, axis_values_json: str) -> bool:
    """
    Instantiate a variable font with frozen coordinates for specified axes.
    input_path: Path to the source variable font file (.ttf / .otf).
    output_path: Path to write the resulting static font file.
    axis_values_json: JSON string mapping axis tags (e.g. 'wght', 'opsz') to float values.
    """
    try:
        if isinstance(axis_values_json, str):
            custom_axes = json.loads(axis_values_json)
        else:
            custom_axes = dict(axis_values_json)

        font = TTFont(input_path)
        fvar = font.get('fvar')
        if not fvar:
            # If not a variable font, save directly
            font.save(output_path)
            return True

        # Build complete dictionary of coordinates for ALL axes in fvar
        # Untouched axes use their defaultValue
        pinned_axes = {}
        for axis in fvar.axes:
            tag = axis.axisTag
            if tag in custom_axes:
                pinned_axes[tag] = float(custom_axes[tag])
            else:
                pinned_axes[tag] = float(axis.defaultValue)

        logger.info(f"Instantiating variable font with axes: {pinned_axes}")

        # Completely instantiate the font into a static font
        instance = instancer.instantiateVariableFont(
            font,
            pinned_axes,
            inplace=False,
            optimize=True
        )

        instance.save(output_path)
        logger.info(f"Successfully froze font to {output_path}")
        return True
    except Exception as e:
        logger.error(f"Failed to freeze font: {e}", exc_info=True)
        raise e
