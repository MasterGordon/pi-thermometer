import time

import spidev as SPI
import SSD1306

import Image
import ImageDraw
import ImageFont

import socket

# Raspberry Pi pin configuration:
RST = 19
# Note the following are only used with SPI:
DC = 16
bus = 0
device = 0

# 128x64 display with hardware SPI:
disp = SSD1306.SSD1306(RST, DC, SPI.SpiDev(bus,device))

# Initialize library.
disp.begin()

# Clear display.
disp.clear()
disp.display()

# Create blank image for drawing.
# Make sure to create image with mode '1' for 1-bit color.
width = disp.width
height = disp.height
image = Image.new('1', (width, height))

# Get drawing object to draw on image.
draw = ImageDraw.Draw(image)

# Draw a black filled box to clear the image.
draw.rectangle((0,0,width,height), outline=0, fill=0)

# Draw some shapes.
# First define some constants to allow easy resizing of shapes.
padding = 1
top = padding
x = padding
# Load default font.
font = ImageFont.load_default()

# Alternatively load a TTF font.
# Some other nice fonts to try: http://www.dafont.com/bitmap.php
# font = ImageFont.truetype('Minecraftia.ttf', 8)

# Write two lines of text.
draw.text((x, top+25), 'Starting Webserver on', font=font, fill=255)
try:
	draw.text((x, top), 'Connected known WLAN', font=font, fill=255)
	draw.text((x, top+35), (([ip for ip in socket.gethostbyname_ex(socket.gethostname())[2] if not ip.startswith("127.")] or [[(s.connect(("8.8.8.8", 53)), s.getsockname()[0], s.close()) for s in [socket.socket(socket.AF_INET, socket.SOCK_DGRAM)]][0][1]]) + ["no IP found"])[0] + ":8080", font=font, fill=255)
except:
	draw.text((x, top+35), "10.0.0.5" + ":8080", font=font, fill=255)
	draw.text((x, top), 'Can\'t find known WLAN', font=font, fill=255)
	draw.text((x, top+15), 'Starting own Hotspot', font=font, fill=255)


# Display image.
disp.image(image)
disp.display()

