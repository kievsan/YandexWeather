# Сервис по получению метеорологических данных

Необходимо получить данные из сервиса Яндекс: https://yandex.ru/dev/weather/doc/ru/concepts/how-to
- Описание API: https://yandex.ru/dev/weather/doc/ru/concepts/forecast-info

1. Сделать GET запрос используя путь: _`https://api.weather.yandex.ru/v2/forecast`_. 
    Передать координаты точки lat и lon, в которой хотите определить погоду, 
    например: `https://api.weather.yandex.ru/v2/forecast?lat=55.75&lon=37.62`.
2. Вывести на экран все данные (весь ответ от сервиса в формате json) и отдельно температуру (находится в fact {temp}).
3. Вычислить среднюю температуру за определенный период (передать limit и найти среднее арифметическое температуры).

---

## для запуска сервиса:

1. Клонировать из github:
    #### git clone
2. Открыть в IDE и запустить

---