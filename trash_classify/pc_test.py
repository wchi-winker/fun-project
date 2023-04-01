import numpy as np
from keras.models import load_model
from tensorflow.keras.utils import img_to_array,load_img

def predict(img_path):
    #导入图片
    image = load_img(img_path)
    image = image.resize((300,300))
    image = img_to_array(image)
    image = image/255
    image = np.expand_dims(image,0)
    result = model.predict(image)
    result = result.argmax(1)
    class_name_pred=classes[result[0]]
    # print(result)
    return class_name_pred

model = load_model('./trash_module.h5')
classes=["cardboard","glass","metal","paper","plastic","trash"]
img_dir = './train_data/paper/paper1.jpg'
predict(img_dir)