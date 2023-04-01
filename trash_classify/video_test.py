#测试
from keras.models import load_model
from keras.preprocessing.image import ImageDataGenerator,img_to_array,load_img
import numpy as np
import os,time,cv2
from PIL import Image

os.environ["CUDA_VISIBLE_DEVICES"] = "-1"
model = load_model('./trash_module.h5')
classes=["cardboard","glass","metal","paper","plastic","trash"]

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


# video = "http://admin:940024@192.168.191.2:8081/"
camera = cv2.VideoCapture(0)
n = 0
while True:
    ret, frame = camera.read()
    cv2.imshow('frame',frame)
    n = n + 1
    # 这个地方得意思是20帧识别一次 每帧都识别的话实时性可能不好
    if n % 20 == 0:
        frame = Image.fromarray(cv2.cvtColor(frame,cv2.COLOR_BGR2RGB))
        predict(frame)

    key = cv2.waitKey(1) & 0xFF
    if key == ord("q"):
            break

camera.release()
cv2.destroyAllWindows()