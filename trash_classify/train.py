import numpy as np
import pandas as pd
import tensorflow as tf
import tensorflow.keras as keras
import cv2 as cv
from tensorflow.keras.optimizers import RMSprop, Adam
from tensorflow.keras.preprocessing.image import ImageDataGenerator

datagen = ImageDataGenerator(rescale=1./255)
train_datagen = datagen.flow_from_directory('../input/garbage-classification/Garbage classification/Garbage classification/',
                           target_size=(300, 300),
                           batch_size=128,
                           class_mode='categorical')

model = tf.keras.models.Sequential([
    tf.keras.layers.Conv2D(64, (3,3), activation='relu', input_shape=(300, 300, 3)),
    tf.keras.layers.MaxPooling2D((2,2)),
    tf.keras.layers.Conv2D(32, (3,3), activation='relu'),
    tf.keras.layers.MaxPooling2D(2,2),
    tf.keras.layers.Conv2D(16, (3,3), activation='relu'),
    tf.keras.layers.MaxPooling2D(2,2),
    tf.keras.layers.Flatten(),
    tf.keras.layers.Dense(512, activation='relu'),
    tf.keras.layers.Dense(6, activation='softmax')
])

model.compile(loss='categorical_crossentropy',
              optimizer=Adam(learning_rate=0.003),
              metrics=['accuracy'])

history = model.fit(
      train_datagen,
      steps_per_epoch=8,  
      epochs=40,
      verbose=1)

model.save('trash_module.h5')