# -*- coding = utf-8 -*-
# @Time: 2024/6/16 8:43
# @Author: Karwei
# @File: convert.PY
# @SoftWare: PyCharm
import mediapipe as mp
from mediapipe.tasks.python.genai import converter

config = converter.ConversionConfig(
  input_ckpt='..\\models\\phi-2\\model\\model*.safetensors',
  ckpt_format='safetensors',
  model_type='PHI_2',
  backend='gpu',
  output_dir='..\\models\\phi-2\\output_dir',
  combine_file_only=False,
  vocab_model_file='..\\models\\phi-2\\tokenizer',
  output_tflite_file='..\\models\\phi-2\\phi_2_gpu.bin'
)

converter.convert_checkpoint(config)
