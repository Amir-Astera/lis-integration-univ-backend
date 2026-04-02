-- Migration: V12 - Consumable Inventory Seed Data
-- Purpose: Comprehensive seed of laboratory consumables for Kazakhstan state hospital LIMS
-- Created: 2026-04-02
-- Note: Only names are seeded - quantities, prices, lot numbers, and expiry dates are managed manually

-- =============================================================================
-- Section 1: Вакуумные пробирки (Vacuum Tubes) - для разных типов анализов
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('tube-purple-4ml', 'Вакуумная пробирка фиолетовая 4 мл (EDTA-K2) - гематология', 'TUBE', 'PURPLE', 'HEMATOLOGY', 'ОАК,общий анализ крови,гематология,ВСК,РВС', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для общего анализа крови (ОАК), гематологических исследований', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tube-purple-2ml', 'Вакуумная пробирка фиолетовая 2 мл (EDTA-K2) - педиатрия', 'TUBE', 'PURPLE', 'HEMATOLOGY', 'ОАК,дети,педиатрия,гематология', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для педиатрических исследований', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tube-blue-2.7ml', 'Вакуумная пробирка голубая 2.7 мл (3.2% Na Citrate) - коагулогия', 'TUBE', 'BLUE', 'COAGULATION', 'коагулогия,ПТИ,АЧТВ,фибриноген,Д-димер,МНО', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для коагулологических исследований', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tube-blue-1.8ml', 'Вакуумная пробирка голубая 1.8 мл (3.2% Na Citrate) - дети', 'TUBE', 'BLUE', 'COAGULATION', 'коагулогия,дети,АЧТВ', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для педиатрической коагулологии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tube-red-5ml', 'Вакуумная пробирка красная 5 мл (без добавок/с активатором свертывания) - биохимия', 'TUBE', 'RED', 'BIOCHEMISTRY', 'биохимия,глюкоза,холестерин,белок,АЛТ,АСТ,креатинин', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для биохимических исследований (сыворотка)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tube-red-3ml', 'Вакуумная пробирка красная 3 мл (с активатором свертывания)', 'TUBE', 'RED', 'BIOCHEMISTRY', 'биохимия,экспресс-анализ', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для ускоренного получения сыворотки', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tube-yellow-5ml', 'Вакуумная пробирка желтая 5 мл (с разделительным гелем и активатором) - иммунология', 'TUBE', 'YELLOW', 'IMMUNOASSAY,POCT_IMMUNOASSAY', 'иммунология,ВИЧ,гепатит,сифилис,гормоны,антитела', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для иммуноферментных исследований', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tube-green-4ml', 'Вакуумная пробирка зеленая 4 мл (Li Heparin) - биохимия', 'TUBE', 'GREEN', 'BIOCHEMISTRY', 'биохимия,гепарин,экспресс', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для плазменной биохимии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tube-gray-2ml', 'Вакуумная пробирка серая 2 мл (Na Fluoride + K Oxalate) - глюкоза', 'TUBE', 'GRAY', 'BIOCHEMISTRY', 'глюкоза,глюкозный,сахар', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для определения глюкозы (стабилизация)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tube-yellow-8.5ml', 'Вакуумная пробирка желтая 8.5 мл (с гелем) - большой объем', 'TUBE', 'YELLOW', 'IMMUNOASSAY,BIOCHEMISTRY', 'иммунология,большой объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для исследований требующих большого объема сыворотки', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 2: Микропробирки (Micro Tubes) - для малых объемов, PCR, Eppendorf
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('microtube-0.5ml', 'Микропробирка 0.5 мл (Eppendorf-style) низкая', 'TUBE_MICRO', NULL, 'HEMATOLOGY,BIOCHEMISTRY', 'микрообъем,дети,плазма', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для микрообъемных исследований', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('microtube-1.5ml', 'Микропробирка 1.5 мл (Eppendorf-style) универсальная', 'TUBE_MICRO', NULL, 'HEMATOLOGY,BIOCHEMISTRY,IMMUNOASSAY', 'микрообъем,центрифуга,хранение', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Стандартная лабораторная микропробирка', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('microtube-2ml', 'Микропробирка 2 мл (Eppendorf-style) для больших объемов', 'TUBE_MICRO', NULL, 'BIOCHEMISTRY,IMMUNOASSAY', 'хранение,алицикотирование', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для хранения образцов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('pcr-tube-0.2ml', 'PCR пробирка 0.2 мл (тоностенная, с плоской крышкой)', 'TUBE_MICRO', NULL, NULL, 'ПЦР,молекулярная биология,ДНК,РНК', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для ПЦР реакций', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('pcr-tube-0.5ml', 'PCR пробирка 0.5 мл (тоностенная)', 'TUBE_MICRO', NULL, NULL, 'ПЦР,молекулярная биология', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для ПЦР реакций большего объема', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('pcr-strip-8-tubes', 'PCR стрип 8 пробирок (0.2 мл каждая, плоская крышка)', 'TUBE_MICRO', NULL, NULL, 'ПЦР,молекулярная биология', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', '8-ми пробирочный стрип для ПЦР', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('pcr-plate-96-well', 'PCR планшет 96-луночный (0.2 мл)', 'WELL_PLATE', NULL, NULL, 'ПЦР,молекулярная биология,Real-time PCR', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', '96-луночный PCR планшет', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('deep-well-plate-96', 'Deep-well планшет 96-луночный (1.0-2.0 мл)', 'WELL_PLATE', NULL, NULL, 'хранение,подготовка образцов', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для хранения и подготовки образцов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('deep-well-plate-24', 'Deep-well планшет 24-луночный (3.5-4.0 мл)', 'WELL_PLATE', NULL, NULL, 'хранение,большой объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для хранения больших объемов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cryotube-1.8ml', 'Криопробирка 1.8 мл (внешняя резьба)', 'TUBE_MICRO', NULL, NULL, 'криохранение,заморозка,долгосрочное хранение', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для долгосрочного хранения при -80°C', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cryotube-4.5ml', 'Криопробирка 4.5 мл (внешняя резьба)', 'TUBE_MICRO', NULL, NULL, 'криохранение,заморозка,большой объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для долгосрочного хранения больших объемов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 3: Контейнеры для биоматериала (Urine, Stool, Sputum containers)
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('urine-container-100ml', 'Контейнер для мочи 100 мл (пластик, винтовая крышка)', 'URINE_CONTAINER', NULL, 'URINALYSIS', 'моча,ОАМ,общий анализ мочи', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Универсальный контейнер для мочи', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('urine-container-60ml', 'Контейнер для мочи 60 мл (пластик)', 'URINE_CONTAINER', NULL, 'URINALYSIS', 'моча,ОАМ', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Экономичный вариант для мочи', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('urine-24h-container', 'Контейнер для суточной мочи 2.5 л (с консервантом)', 'URINE_CONTAINER', NULL, 'URINALYSIS,BIOCHEMISTRY', 'суточная моча,протеинурия,креатинин', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'С контсервантом для суточной мочи', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('urine-24h-container-no-preservative', 'Контейнер для суточной мочи 2.5 л (без консерванта)', 'URINE_CONTAINER', NULL, 'URINALYSIS', 'суточная моча,без консерванта', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Без консерванта для определенных анализов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('stool-container-30ml', 'Контейнер для кала 30 мл (пластик, ложка-скрепка)', 'STOOL_CONTAINER', NULL, NULL, 'кал,копрология,паразиты,яйца глист', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Стандартный контейнер для кала', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('stool-container-60ml', 'Контейнер для кала 60 мл (с консервантом)', 'STOOL_CONTAINER', NULL, NULL, 'кал,бакпосев,микробиология', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для микробиологических исследований кала', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('sputum-container-50ml', 'Контейнер для мокроты 50 мл (пластик, широкое горло)', 'CONTAINER', NULL, NULL, 'мокрота,бакпосев,туберкулез,АБ', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для сбора мокроты на бакпосев', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('sputum-container-sterile', 'Контейнер для мокроты стерильный (широкое горло)', 'CONTAINER', NULL, NULL, 'мокрота,бакпосев,стерильный', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Стерильный контейнер для мокроты', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('csf-container-sterile', 'Контейнер для ЛЦЖ (спинномозговой жидкости) стерильный', 'CONTAINER', NULL, NULL, 'ЛЦЖ,спинномозговая жидкость,нейроинфекция', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для сбора и транспортировки ЛЦЖ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('wound-swab-container', 'Контейнер для ватной палочки (транспортный)', 'CONTAINER', NULL, NULL, 'мазок,бакпосев,рана,гной', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Транспортный контейнер для мазков', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('histo-container-20ml', 'Контейнер для гистологического материала 20 мл (10% формалин)', 'CONTAINER', NULL, NULL, 'гистология,биоптат,формалин', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'С формалином для гистологии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('histo-container-60ml', 'Контейнер для гистологического материала 60 мл (10% формалин)', 'CONTAINER', NULL, NULL, 'гистология,большой биоптат', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Большой контейнер с формалином', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('semen-container', 'Контейнер для спермы (стерильный, нейтральный)', 'CONTAINER', NULL, NULL, 'сперма,спермограмма,фертильность', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для спермограммы', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('saliva-container', 'Контейнер для слюны (с консервантом)', 'CONTAINER', NULL, NULL, 'слюна,DNA,генетика', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для сбора слюны на ДНК', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 4: Наконечники для дозаторов (Pipette Tips) - разные объемы
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('tip-10ul', 'Наконечник 10 мкл (ультрамикро, отфильтрованный)', 'TIP', NULL, NULL, 'микрообъем,ПЦР,ДНК', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для объемов 0.5-10 мкл', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tip-10ul-extended', 'Наконечник 10 мкл удлиненный (для PCR планшетов)', 'TIP', NULL, NULL, 'ПЦР,планшеты', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Удлиненный для доступа к дну планшетов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tip-20ul', 'Наконечник 20 мкл (микро)', 'TIP', NULL, NULL, 'микрообъем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для объемов 2-20 мкл', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tip-100ul', 'Наконечник 100 мкл (микро)', 'TIP', NULL, NULL, 'микрообъем,иммунология', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для объемов 10-100 мкл', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tip-200ul', 'Наконечник 200 мкл (стандартный желтый)', 'TIP', NULL, 'HEMATOLOGY,BIOCHEMISTRY,IMMUNOASSAY', 'стандартный,Универсальный', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Самый распространенный наконечник, 20-200 мкл', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tip-200ul-filtered', 'Наконечник 200 мкл с фильтром (желтый)', 'TIP', NULL, NULL, 'стерильный,ПЦР,без ДНКаз/РНКаз', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Фильтрованный для чувствительных работ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tip-1000ul', 'Наконечник 1000 мкл (синий, 1 мл)', 'TIP', NULL, 'HEMATOLOGY,BIOCHEMISTRY', 'средний объем,разведение', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для объемов 100-1000 мкл', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tip-1000ul-filtered', 'Наконечник 1000 мкл с фильтром (синий)', 'TIP', NULL, NULL, 'стерильный,клеточная культура', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Фильтрованный для клеточных работ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tip-5000ul', 'Наконечник 5000 мкл (5 мл)', 'TIP', NULL, NULL, 'большой объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для объемов до 5 мл', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tip-10000ul', 'Наконечник 10000 мкл (10 мл)', 'TIP', NULL, NULL, 'большой объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для объемов до 10 мл', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tip-multichannel-12', 'Наконечники для 12-канального дозатора (200 мкл)', 'TIP', NULL, NULL, 'мультиканальный,планшеты', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'В ленте для мультиканальных дозаторов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tip-multichannel-8', 'Наконечники для 8-канального дозатора (200 мкл)', 'TIP', NULL, NULL, 'мультиканальный,планшеты', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'В ленте для мультиканальных дозаторов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tip-box-empty', 'Штатив для наконечников (пустой, 96 ячеек)', 'PIPETTE_TIP_BOX', NULL, NULL, 'хранение,наконечники', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Пластиковый штатив для наконечников', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tip-refill-200ul', 'Наконечники 200 мкл в мешке (для перезаправки штативов)', 'TIP', NULL, NULL, 'экономичный,перезаправка', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Экономичный вариант без штативов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 5: Пипетки одноразовые (Pipettes - Pasteur, serological)
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('pipette-pasteur-1ml', 'Пипетка Пастера 1 мл (стекло или пластик)', 'PIPETTE', NULL, NULL, 'перенос жидкостей', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Универсальная одноразовая пипетка', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('pipette-pasteur-2ml', 'Пипетка Пастера 2 мл (стекло или пластик)', 'PIPETTE', NULL, NULL, 'перенос жидкостей', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Большая пипетка Пастера', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('pipette-pasteur-3ml', 'Пипетка Пастера 3 мл (стекло или пластик)', 'PIPETTE', NULL, NULL, 'перенос жидкостей', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Максимальный объем пипетки Пастера', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('pipette-serological-1ml', 'Пипетка серологическая 1 мл (стерильная, градуированная)', 'PIPETTE', NULL, NULL, 'серология,точный объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Точная градуированная пипетка', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('pipette-serological-2ml', 'Пипетка серологическая 2 мл (стерильная, градуированная)', 'PIPETTE', NULL, NULL, 'серология,точный объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Точная градуированная пипетка', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('pipette-serological-5ml', 'Пипетка серологическая 5 мл (стерильная, градуированная)', 'PIPETTE', NULL, NULL, 'серология,точный объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Точная градуированная пипетка', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('pipette-serological-10ml', 'Пипетка серологическая 10 мл (стерильная, градуированная)', 'PIPETTE', NULL, NULL, 'серология,точный объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Точная градуированная пипетка', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('pipette-serological-25ml', 'Пипетка серологическая 25 мл (стерильная)', 'PIPETTE', NULL, NULL, 'серология,большой объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для больших объемов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('pipette-serological-50ml', 'Пипетка серологическая 50 мл (стерильная)', 'PIPETTE', NULL, NULL, 'серология,большой объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для очень больших объемов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('pipette-bulb', 'Груша резиновая для пипеток Пастера', 'PIPETTE', NULL, NULL, 'груша,пипетка Пастера', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Резиновая груша для набора жидкости', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 6: Шприцы медицинские (Syringes)
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('syringe-1ml-tb', 'Шприц инъекционный 1 мл (Туберкулиновый)', 'SYRINGE', NULL, NULL, 'инъекции,вакцинация,малые объемы', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Точный шприц для малых объемов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('syringe-2ml', 'Шприц инъекционный 2 мл (3-х компонентный)', 'SYRINGE', NULL, NULL, 'инъекции,внутримышечные', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Стандартный шприц', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('syringe-5ml', 'Шприц инъекционный 5 мл (3-х компонентный)', 'SYRINGE', NULL, NULL, 'инъекции,внутримышечные', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для средних объемов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('syringe-10ml', 'Шприц инъекционный 10 мл (3-х компонентный)', 'SYRINGE', NULL, NULL, 'инъекции,внутривенные', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для больших объемов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('syringe-20ml', 'Шприц инъекционный 20 мл (3-х компонентный)', 'SYRINGE', NULL, NULL, 'инъекции,внутривенные,большой объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для больших объемов инъекций', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('syringe-50ml', 'Шприц инъекционный 50 мл (катетерный наконечник)', 'SYRINGE', NULL, NULL, 'промывание,катетер', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для промывания катетеров', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('syringe-10ml-luer-lock', 'Шприц 10 мл с Luer-Lock (съемная игла)', 'SYRINGE', NULL, NULL, 'безопасность,фильтрация', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'С закручивающимся наконечником', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('syringe-20ml-luer-lock', 'Шприц 20 мл с Luer-Lock (съемная игла)', 'SYRINGE', NULL, NULL, 'безопасность,фильтрация', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'С закручивающимся наконечником', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('syringe-50ml-luer-lock', 'Шприц 50 мл с Luer-Lock (съемная игла)', 'SYRINGE', NULL, NULL, 'безопасность,фильтрация', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'С закручивающимся наконечником', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('syringe-insulin-1ml-u40', 'Шприц инсулиновый 1 мл U-40 (40 ед/мл)', 'SYRINGE', NULL, NULL, 'инсулин,диабет,U40', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для инсулина U-40', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('syringe-insulin-1ml-u100', 'Шприц инсулиновый 1 мл U-100 (100 ед/мл)', 'SYRINGE', NULL, NULL, 'инсулин,диабет,U100', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для инсулина U-100', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 7: Иглы медицинские (Needles)
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('needle-16g-1.5', 'Игла инъекционная 16G (1.6x38 мм) - большой калибр', 'NEEDLE', NULL, NULL, 'внутривенные,забор крови,гемотрансфузия', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Очень толстая игла для быстрого забора', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('needle-18g-1.5', 'Игла инъекционная 18G (1.2x38 мм) - крупный калибр', 'NEEDLE', NULL, 'HEMATOLOGY,BIOCHEMISTRY', 'внутривенные,забор крови,гемотрансфузия', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Толстая игла для гемотрансфузии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('needle-20g-1.5', 'Игла инъекционная 20G (0.9x38 мм) - средний калибр', 'NEEDLE', NULL, 'HEMATOLOGY,BIOCHEMISTRY', 'внутривенные,внутримышечные,общий анализ', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Стандартная игла для взрослых', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('needle-21g-1.5', 'Игла инъекционная 21G (0.8x38 мм) - средний калибр', 'NEEDLE', NULL, 'HEMATOLOGY,BIOCHEMISTRY', 'внутривенные,внутримышечные', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Чуть тоньше стандартной', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('needle-22g-1.5', 'Игла инъекционная 22G (0.7x38 мм) - тонкий калибр', 'NEEDLE', NULL, 'HEMATOLOGY,BIOCHEMISTRY', 'внутривенные,дети,пожилые', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Тонкая игла для детей и пожилых', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('needle-23g-1', 'Игла инъекционная 23G (0.6x25 мм) - очень тонкая', 'NEEDLE', NULL, NULL, 'внутридермальные,микроинъекции', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для микроинъекций', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('needle-25g-1', 'Игла инъекционная 25G (0.5x25 мм) - микроигла', 'NEEDLE', NULL, NULL, 'подкожные,инсулин,дети', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для инсулиновых инъекций', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('needle-27g-0.5', 'Игла инъекционная 27G (0.4x13 мм) - ультратонкая', 'NEEDLE', NULL, NULL, 'внутридермальные,косметология', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Ультратонкая для чувствительных пациентов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('needle-butterfly-23g', 'Игла-бабочка (катетер-бабочка) 23G (0.6x19 мм)', 'BUTTERFLY', NULL, 'HEMATOLOGY,BIOCHEMISTRY', 'внутривенные,дети,труднодоступные вены', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для труднодоступных вен и детей', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('needle-butterfly-25g', 'Игла-бабочка (катетер-бабочка) 25G (0.5x19 мм)', 'BUTTERFLY', NULL, 'HEMATOLOGY,BIOCHEMISTRY', 'внутривенные,дети,микрообъем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для микрообъемов и детей', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('needle-butterfly-21g', 'Игла-бабочка (катетер-бабочка) 21G (0.8x19 мм)', 'BUTTERFLY', NULL, 'HEMATOLOGY,BIOCHEMISTRY', 'внутривенные,взрослые', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для взрослых с хорошими венами', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('needle-lancet-21g', 'Скарификатор (ланцет) 21G для прокола пальца', 'LANCET', NULL, 'HEMATOLOGY', 'скарификатор,палец,капиллярная кровь,глюкометр', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для забора капиллярной крови', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('needle-lancet-23g', 'Скарификатор (ланцет) 23G для прокола пальца', 'LANCET', NULL, 'HEMATOLOGY', 'скарификатор,палец,капиллярная кровь,дети', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Тонкий скарификатор для детей', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('needle-lancet-28g', 'Скарификатор (ланцет) 28G для прокола пальца', 'LANCET', NULL, 'HEMATOLOGY', 'скарификатор,палец,капиллярная кровь,микрообъем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Ультратонкий для минимальной боли', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('needle-holder-vacuum', 'Держатель для вакуумных систем (пробирок)', 'HOLDER', NULL, 'HEMATOLOGY,BIOCHEMISTRY', 'вакуум,забор крови,Vacutainer', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Универсальный держатель для вакуумных пробирок', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('needle-holder-soft', 'Держатель мягкий для вакуумных систем', 'HOLDER', NULL, 'HEMATOLOGY', 'вакуум,эргономичный', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Мягкий держатель для удобства', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tourniquet-disposable', 'Жгут кровоостанавливающий одноразовый (латекс/резина)', 'TOURNIQUET', NULL, 'HEMATOLOGY,BIOCHEMISTRY', 'жгут,вены,забор крови', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Одноразовый жгут для венепункции', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tourniquet-reusable', 'Жгут кровоостанавливающий многоразовый', 'TOURNIQUET', NULL, 'HEMATOLOGY,BIOCHEMISTRY', 'жгут,вены,забор крови', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Многоразовый жгут (дезинфекция между пациентами)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 8: Кюветы и реакционные сосуды (Cuvettes, Reaction Vessels)
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('cuvette-photometer-1cm', 'Кювета фотометрическая пластиковая 1 см (VIS)', 'CUVETTE', NULL, 'BIOCHEMISTRY', 'фотометр,спектрофотометр,цветная реакция', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для фотометрических измерений в видимом диапазоне', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cuvette-photometer-uv', 'Кювета кварцевая/UV пластик 1 см (UV-VIS)', 'CUVETTE', NULL, 'BIOCHEMISTRY', 'спектрофотометр,УФ,280nm,260nm', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для УФ-измерений (кварц или спец. пластик)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cuvette-micro-0.5ml', 'Микрокювета 0.5 мл (малый объем)', 'CUVETTE', NULL, 'BIOCHEMISTRY', 'микрообъем,фотометр', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для малых объемов образцов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cuvette-micro-0.1ml', 'Микрокювета 0.1 мл (ультрамикро)', 'CUVETTE', NULL, 'BIOCHEMISTRY', 'ультрамикрообъем,фотометр', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для очень малых объемов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cuvette-bc5000', 'Кювета для BC-5000/BC-5150 Mindray', 'CUVETTE', 'HEMATOLOGY', NULL, 'BC-5000,гематология,Mindray', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Оригинальная кювета для гематологического анализатора', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cuvette-coagulation', 'Кювета для коагулометра (C3100)', 'CUVETTE', 'COAGULATION', NULL, 'коагулометр,коагулогия,C3100', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Специальная кювета для коагулологических тестов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cuvette-esr', 'Кювета для СОЭ (Westergren или автомат)', 'CUVETTE', NULL, 'ESR', 'СОЭ,осадка эритроцитов,Vision Pro', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для определения скорости оседания эритроцитов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('reaction-vessel-biochem', 'Реакционная ячейка для биохимического анализатора', 'REACTION_VESSEL', NULL, 'BIOCHEMISTRY', 'BS-240,BS-430,Mindray,биохимия', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Одноразовая или многоразовая ячейка для Mindray BS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('reaction-cup-iflash', 'Реакционная кювета для iFlash 1800', 'REACTION_VESSEL', NULL, 'IMMUNOASSAY', 'iFlash,иммунология,хемилюминесценция', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для хемилюминесцентного иммуноанализатора', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('well-plate-96-flat', 'Планшет 96-луночный плоскодонный (Flat bottom)', 'WELL_PLATE', NULL, NULL, 'ИФА,ELISA,иммунология', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для иммуноферментных анализов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('well-plate-96-round', 'Планшет 96-луночный круглодонный (U-bottom)', 'WELL_PLATE', NULL, NULL, 'иммунология,осаждение', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для иммунопреципитации', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('well-plate-96-v', 'Планшет 96-луночный V-образное дно', 'WELL_PLATE', NULL, NULL, 'иммунология,гемагглютинация', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для гемагглютинации', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('well-plate-384', 'Планшет 384-луночный высокопроизводительный', 'WELL_PLATE', NULL, NULL, 'скрининг,высокая плотность', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для высокопроизводительного скрининга', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('well-plate-24', 'Планшет 24-луночный (клеточная культура)', 'WELL_PLATE', NULL, NULL, 'клеточная культура,клетки', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для клеточной культуры', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('well-plate-6', 'Планшет 6-луночный (крупная клеточная культура)', 'WELL_PLATE', NULL, NULL, 'клеточная культура,большой объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для крупных клеточных культур', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 9: Гель-карты и иммуногематология (Gel Cards for ORTHO Workstation)
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('gel-card-abo-rh', 'Гель-карта АВО и Rh(D) (карта определения группы крови)', 'CARD', NULL, 'IMMUNOHEMATOLOGY', 'группа крови,ABO,Rh,резус,ORTHO', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для определения группы крови и резус-фактора', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gel-card-abo-confirm', 'Гель-карта обратная группа крови АВО (плазма)', 'CARD', NULL, 'IMMUNOHEMATOLOGY', 'обратная группа крови,плазма,антитела', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Подтверждение группы крови через плазму', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gel-card-phenotype', 'Гель-карта фенотипирование (расширенное)', 'CARD', NULL, 'IMMUNOHEMATOLOGY', 'фенотип,келл,дюффи,кром', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для расширенного фенотипирования эритроцитов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gel-card-crossmatch', 'Гель-карта совместимость крови (crossmatch)', 'CARD', NULL, 'IMMUNOHEMATOLOGY', 'совместимость,переливание,кроссматч', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для проверки совместимости донорской крови', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gel-card-antibody-screen', 'Гель-карта скрининг антител (3 клетки)', 'CARD', NULL, 'IMMUNOHEMATOLOGY', 'антитела,скрининг,IAT,неправильные антитела', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для скрининга неправильных антител', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gel-card-antibody-id', 'Гель-карта идентификация антител (11 клеток)', 'CARD', NULL, 'IMMUNOHEMATOLOGY', 'идентификация антител,панель', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для идентификации специфичности антител', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gel-card-dat', 'Гель-карта прямая проба Кумбса (DAT)', 'CARD', NULL, 'IMMUNOHEMATOLOGY', 'DAT,прямая проба Кумбса,гемолитическая анемия', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для прямой антиглобулиновой пробы', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gel-card-iat', 'Гель-карта непрямая проба Кумбса (IAT)', 'CARD', NULL, 'IMMUNOHEMATOLOGY', 'IAT,непрямая проба Кумбса', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для непрямой антиглобулиновой пробы', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gel-card-neutrophil', 'Гель-карта гранулоцитарные антитела', 'CARD', NULL, 'IMMUNOHEMATOLOGY', 'гранулоциты,нейтрофилы,HNA', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для определения антител к гранулоцитам', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gel-card-platelet', 'Гель-карта тромбоцитарные антитела (HPA)', 'CARD', NULL, 'IMMUNOHEMATOLOGY', 'тромбоциты,HPA,тромбоцитарные антитела', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для определения антител к тромбоцитам', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('ortho-wash-solution', 'Раствор для промывки ORTHO Workstation', 'BUFFER', NULL, 'IMMUNOHEMATOLOGY', 'промывка,ORTHO,системный раствор', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Расходник для автоматической промывки системы', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('ortho-control-cells', 'Контрольные эритроциты для иммуногематологии', 'QC_MATERIAL', NULL, 'IMMUNOHEMATOLOGY', 'контроль,эритроциты,квалитет', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Контрольный материал для валидации тестов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 10: Картриджи и дискретные системы (Cartridges for POCT analyzers)
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('cartridge-u500-10', 'Тест-полоска Mission U500 (10 параметров мочи)', 'STRIP', NULL, 'URINALYSIS', 'U500,моча,общий анализ мочи', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для анализатора мочи Mission U500', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cartridge-ocg-102-pt', 'Картридж OCG-102 PT (Протромбиновое время)', 'CARTRIDGE', NULL, 'POCT_COAGULATION', 'OCG-102,коагулогия,ПТИ,ПТ', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'POCT тест на протромбиновое время', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cartridge-ocg-102-aptt', 'Картридж OCG-102 APTT (Активированное ЧВ)', 'CARTRIDGE', NULL, 'POCT_COAGULATION', 'OCG-102,коагулогия,АЧТВ,APTT', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'POCT тест на АЧТВ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cartridge-ocg-102-tt', 'Картридж OCG-102 TT (Тромбиновое время)', 'CARTRIDGE', NULL, 'POCT_COAGULATION', 'OCG-102,коагулогия,ТВ,тромбин', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'POCT тест на тромбиновое время', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cartridge-ocg-102-fib', 'Картридж OCG-102 FIB (Фибриноген)', 'CARTRIDGE', NULL, 'POCT_COAGULATION', 'OCG-102,коагулогия,фибриноген', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'POCT тест на фибриноген', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cartridge-ocg-102-ddimer', 'Картридж OCG-102 D-Dimer (Д-димер)', 'CARTRIDGE', NULL, 'POCT_COAGULATION', 'OCG-102,коагулогия,Д-димер,D-dimer', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'POCT тест на D-димер', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cartridge-edan-i15-basic', 'Картридж Edan i15 базовый (pH, pCO2, pO2)', 'CARTRIDGE', NULL, 'BLOOD_GAS', 'i15,газы крови, acid-base', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Базовая панель газов крови', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cartridge-edan-i15-extended', 'Картридж Edan i15 расширенный (газы + электролиты)', 'CARTRIDGE', NULL, 'BLOOD_GAS', 'i15,газы крови,Na,K,Cl,ionized calcium', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Расширенная панель с электролитами', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cartridge-edan-i15-glucose', 'Картридж Edan i15 с глюкозой и лактатом', 'CARTRIDGE', NULL, 'BLOOD_GAS', 'i15,глюкоза,лактат,газы крови', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Панель с глюкозой и лактатом', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cartridge-fluorecare-crp', 'Картридж Fluorecare CRP', 'CARTRIDGE', NULL, 'POCT_IMMUNOASSAY', 'Fluorecare,CRP,C-реактивный белок', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'POCT тест на CRP', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cartridge-fluorecare-dimer', 'Картридж Fluorecare D-Dimer', 'CARTRIDGE', NULL, 'POCT_IMMUNOASSAY', 'Fluorecare,D-димер', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'POCT тест на D-димер', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cartridge-fluorecare-trop', 'Картридж Fluorecare Troponin I', 'CARTRIDGE', NULL, 'POCT_IMMUNOASSAY', 'Fluorecare,тропонин,ИМ', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'POCT тест на тропонин I', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cartridge-fluorecare-ngal', 'Картридж Fluorecare NGAL', 'CARTRIDGE', NULL, 'POCT_IMMUNOASSAY', 'Fluorecare,NGAL,почечная функция', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'POCT тест на NGAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cassette-iflash-50', 'Кассета iFlash 1800 (50 тестов)', 'CASSETTE', NULL, 'IMMUNOASSAY', 'iFlash,кассета,хемилюминесценция', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Кассета для хемилюминесцентного анализатора', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('chip-iflash-magnetic', 'Магнитные чипы для iFlash 1800', 'CHIP', NULL, 'IMMUNOASSAY', 'iFlash,магнитные чипы,носитель', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Магнитные чипы-носители для реакций', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cassette-iflash-reaction-cup', 'Реакционные кюветы для iFlash (упаковка)', 'REACTION_VESSEL', NULL, 'IMMUNOASSAY', 'iFlash,реакционные кюветы', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Одноразовые реакционные кюветы', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cassette-iflash-sample-tip', 'Наконечники для образцов iFlash (упаковка)', 'TIP', NULL, 'IMMUNOASSAY', 'iFlash,наконечники,дозирование', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Наконечники для дозирования образцов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 11: Микроскопия - предметные и покровные стёкла (Slides)
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('slide-plain-76x26', 'Предметное стекло 76x26 мм (неполированное)', 'SLIDE', NULL, NULL, 'микроскопия,мазок,гистология', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Стандартное предметное стекло', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('slide-polished-76x26', 'Предметное стекло 76x26 мм (полированное края)', 'SLIDE', NULL, NULL, 'микроскопия,безопасность', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'С закругленными краями для безопасности', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('slide-frosted-76x26', 'Предметное стекло 76x26 мм с матовым полем (для надписи)', 'SLIDE', NULL, NULL, 'микроскопия,маркировка,матовое поле', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'С матовым краем для карандашной маркировки', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('slide-adhesive', 'Предметное стекло с адгезивным покрытием (positively charged)', 'SLIDE', NULL, NULL, 'гистология,иммуногистохимия,IHC', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для иммуногистохимии и гистологии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('slide-cavity', 'Предметное стекло с углублением (для висячих капель)', 'SLIDE', NULL, NULL, 'микробиология,висячая капля', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'С углублением для микробиологических препаратов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('slide-hematology-counting', 'Предметное стекло для подсчета клеток (с сеткой)', 'SLIDE', NULL, 'HEMATOLOGY', 'подсчет клеток,сетка,Горяева,Фукс-Розенталя', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'С нанесенной сеткой для ручного подсчета', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('coverslip-18x18', 'Покровное стекло 18x18 мм', 'COVER_SLIP', NULL, NULL, 'микроскопия,покровное стекло', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Стандартный размер для мазков', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('coverslip-20x20', 'Покровное стекло 20x20 мм', 'COVER_SLIP', NULL, NULL, 'микроскопия,покровное стекло', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для больших препаратов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('coverslip-22x22', 'Покровное стекло 22x22 мм', 'COVER_SLIP', NULL, NULL, 'микроскопия,покровное стекло', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Популярный размер', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('coverslip-24x24', 'Покровное стекло 24x24 мм', 'COVER_SLIP', NULL, NULL, 'микроскопия,покровное стекло', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для больших препаратов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('coverslip-24x50', 'Покровное стекло 24x50 мм (длинное)', 'COVER_SLIP', NULL, NULL, 'микроскопия,гистология,большой препарат', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для длинных гистологических срезов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('coverslip-22x40', 'Покровное стекло 22x40 мм', 'COVER_SLIP', NULL, NULL, 'микроскопия,гематология', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для гематологических мазков', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('coverslip-thickness-1', 'Покровное стекло толщины №1 (0.13-0.16 мм)', 'COVER_SLIP', NULL, NULL, 'микроскопия,высокая четкость', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Оптимальная толщина для микрофотографии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 12: Средства индивидуальной защиты (PPE - Gloves, Masks, etc.)
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('glove-nitrile-s', 'Перчатки нитриловые диагностические размер S (6-7)', 'GLOVE', NULL, NULL, 'нитрил,перчатки,защита,диагностические', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Нитриловые перчатки без пудры, размер S', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('glove-nitrile-m', 'Перчатки нитриловые диагностические размер M (7-8)', 'GLOVE', NULL, NULL, 'нитрил,перчатки,защита,диагностические', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Нитриловые перчатки без пудры, размер M', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('glove-nitrile-l', 'Перчатки нитриловые диагностические размер L (8-9)', 'GLOVE', NULL, NULL, 'нитрил,перчатки,защита,диагностические', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Нитриловые перчатки без пудры, размер L', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('glove-nitrile-xl', 'Перчатки нитриловые диагностические размер XL (9-10)', 'GLOVE', NULL, NULL, 'нитрил,перчатки,защита,диагностические', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Нитриловые перчатки без пудры, размер XL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('glove-latex-s', 'Перчатки латексные диагностические размер S', 'GLOVE', NULL, NULL, 'латекс,перчатки,эластичные', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Латексные перчатки с высокой эластичностью', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('glove-latex-m', 'Перчатки латексные диагностические размер M', 'GLOVE', NULL, NULL, 'латекс,перчатки,эластичные', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Латексные перчатки с высокой эластичностью', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('glove-latex-l', 'Перчатки латексные диагностические размер L', 'GLOVE', NULL, NULL, 'латекс,перчатки,эластичные', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Латексные перчатки с высокой эластичностью', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('glove-vinyl-s', 'Перчатки виниловые размер S', 'GLOVE', NULL, NULL, 'винил,перчатки,гипоаллергенные', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Виниловые для аллергиков на латекс', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('glove-vinyl-m', 'Перчатки виниловые размер M', 'GLOVE', NULL, NULL, 'винил,перчатки,гипоаллергенные', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Виниловые для аллергиков на латекс', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('glove-vinyl-l', 'Перчатки виниловые размер L', 'GLOVE', NULL, NULL, 'винил,перчатки,гипоаллергенные', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Виниловые для аллергиков на латекс', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('glove-chemo-nitrile-m', 'Перчатки химостойкие нитриловые (для химреактивов)', 'GLOVE', NULL, NULL, 'химостойкие,нитрил,химреактивы', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Усиленные для работы с агрессивными веществами', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('mask-surgical-3ply', 'Маска хирургическая 3-слойная на резинках', 'MASK', NULL, NULL, 'маска,хирургическая,защита', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Стандартная медицинская маска', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('mask-surgical-ties', 'Маска хирургическая 3-слойная на завязках', 'MASK', NULL, NULL, 'маска,хирургическая,завязки', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'С завязками для надежной фиксации', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('mask-n95', 'Респиратор N95/FFP2 (высокая защита)', 'MASK', NULL, NULL, 'респиратор,N95,FFP2,высокая защита', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для работы с инфекционными материалами', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('mask-shield', 'Щиток защитный для лица (Face shield)', 'SHIELD', NULL, NULL, 'щиток,защита лица,брызги', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Защита глаз и лица от брызг', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('mask-shield-reusable', 'Щиток защитный многоразовый (сменные экраны)', 'SHIELD', NULL, NULL, 'щиток,многоразовый,экономичный', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Многоразовый с сменными защитными экранами', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gown-disposable-l', 'Халат одноразовый нетканый размер L', 'GOWN', NULL, NULL, 'халат,одноразовый,защита,посетитель', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для посетителей и общей защиты', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gown-disposable-xl', 'Халат одноразовый нетканый размер XL', 'GOWN', NULL, NULL, 'халат,одноразовый,защита', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для посетителей и общей защиты', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gown-isolation-yellow', 'Халат изоляционный желтый (жидкостойкий)', 'GOWN', NULL, NULL, 'халат,изоляционный,жидкостойкий,заражение', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Жидкостойкий для инфекционных материалов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gown-chemo', 'Халат для работы с цитостатиками (химия)', 'GOWN', NULL, NULL, 'халат,цитостатики,химия,защита', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Специальный для работы с химпрепаратами', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cap-disposable', 'Шапочка одноразовая нетканая (белая/зеленая)', 'CAP', NULL, NULL, 'шапочка,одноразовая,гигиена', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для покрытия волос', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cap-shoe-covers', 'Бахилы одноразовые (пара)', 'CAP', NULL, NULL, 'бахилы,обувь,защита,одноразовые', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для защиты полов и обуви', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('sleeve-covers', 'Нарукавники одноразовые (пара)', 'CAP', NULL, NULL, 'нарукавники,защита,рукава', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для защиты рукавов халата', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 13: Расходники для очистки и дезинфекции (Wipes, Cotton, Disinfectants)
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('wipe-alcohol-70', 'Салфетка спиртовая 70% (индивидуальная упаковка)', 'WIPE', NULL, NULL, 'спирт,дезинфекция,кожа', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для дезинфекции кожи перед забором', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('wipe-surface-disinfectant', 'Салфетка для поверхностей (дезинфицирующая)', 'WIPE', NULL, NULL, 'поверхности,дезинфекция,стол', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для дезинфекции рабочих поверхностей', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('wipe-bleach', 'Салфетка с хлором (для крови и биоматериалов)', 'WIPE', NULL, NULL, 'хлор,кровь,биоматериал,дезинфекция', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для обеззараживания крови и выделений', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('wipe-microfiber', 'Салфетка микрофибра (многоразовая, для протирки)', 'WIPE', NULL, NULL, 'микрофибра,протирка,качественная очистка', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для бережной очистки оптики и поверхностей', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('wipe-lens', 'Салфетка для оптики (линзы, микроскоп)', 'WIPE', NULL, NULL, 'оптика,микроскоп,линза,очистка', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Специальная для оптических поверхностей', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cotton-ball-sterile', 'Ватный шарик стерильный (пакет)', 'COTTON', NULL, NULL, 'вата,шарик,стерильный,обработка', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Стерильный ватный шарик', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cotton-ball-nonsterile', 'Ватный шарик нестерильный (пакет)', 'COTTON', NULL, NULL, 'вата,шарик,очистка', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Нестерильный для общих целей', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cotton-stick-wood', 'Ватная палочка с деревянной ручкой (стерильная)', 'COTTON', NULL, NULL, 'палочка,ватная,мазок,дерево', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для мазков и обработки', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cotton-stick-plastic', 'Ватная палочка с пластиковой ручкой (стерильная)', 'COTTON', NULL, NULL, 'палочка,ватная,мазок,пластик', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Пластиковая ручка для микробиологии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('cotton-stick-aluminum', 'Ватная палочка с алюминиевой ручкой (беззольная)', 'COTTON', NULL, NULL, 'палочка,ватная,беззольная,микробиология', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Беззольная для микробиологических посевов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('sponge-surgical', 'Губка хирургическая стерильная', 'SPONGE', NULL, NULL, 'губка,хирургия,стерильная', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для операционных и процедурных', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gauze-sterile-10x10', 'Марля медицинская стерильная 10x10 см (сложенная)', 'GAUZE', NULL, NULL, 'марля,стерильная,повязка', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Стандартный размер марлевой салфетки', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gauze-sterile-7.5x7.5', 'Марля медицинская стерильная 7.5x7.5 см', 'GAUZE', NULL, NULL, 'марля,стерильная,малый размер', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Меньший размер для малых ран', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gauze-nonsterile-roll', 'Марля медицинская нестерильная в рулоне', 'GAUZE', NULL, NULL, 'марля,рулон,бинт', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Нестерильная марля в рулоне', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gauze-bandage-sterile', 'Бинт марлевый стерильный 5 м х 10 см', 'GAUZE', NULL, NULL, 'бинт,марля,стерильный,повязка', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Стерильный бинт для перевязок', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('band-aid-assorted', 'Пластырь бактерицидный (набор размеров)', 'GAUZE', NULL, NULL, 'пластырь,рана,бактерицидный', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Набор пластырей разных размеров', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('disinfectant-alcohol-70-500ml', 'Спирт этиловый 70% 500 мл (для дезинфекции)', 'DISINFECTANT', NULL, NULL, 'спирт,70%,дезинфекция,этиловый', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для дезинфекции поверхностей и рук', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('disinfectant-alcohol-70-1l', 'Спирт этиловый 70% 1 л', 'DISINFECTANT', NULL, NULL, 'спирт,70%,дезинфекция', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Большой флакон спирта', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('disinfectant-isopropanol', 'Изопропиловый спирт 99% 500 мл', 'DISINFECTANT', NULL, NULL, 'изопропанол,99%,очистка,обезжиривание', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для обезжиривания и очистки', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('disinfectant-chlorine-tablets', 'Таблетки хлора для приготовления раствора (дезинфекция)', 'DISINFECTANT', NULL, NULL, 'хлор,таблетки,дезинфекция,обеззараживание', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для приготовления хлорных растворов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('disinfectant-surface-spray', 'Дезинфицирующее средство для поверхностей (спрей)', 'DISINFECTANT', NULL, NULL, 'спрей,поверхности,дезинфекция', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Готовый к применению спрей', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('disinfectant-hand-wash', 'Мыло антибактериальное для рук (жидкое)', 'DISINFECTANT', NULL, NULL, 'мыло,руки,антисептик', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Жидкое мыло с антибактериальным эффектом', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('disinfectant-hand-gel', 'Антисептик для рук (гель с спиртом)', 'DISINFECTANT', NULL, NULL, 'антисептик,гель,руки,спирт', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Гель для быстрой дезинфекции рук', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('disinfectant-instrument', 'Средство для дезинфекции инструментов (концентрат)', 'DISINFECTANT', NULL, NULL, 'инструменты,дезинфекция,концентрат', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для замачивания инструментов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 14: Упаковочные материалы (Bags, Labels, Tapes)
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('bag-sterile-small', 'Пакет стерильный бумажно-пленочный 100x200 мм', 'BAG', NULL, NULL, 'стерильный,пакет,инструменты', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для стерилизации инструментов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('bag-sterile-medium', 'Пакет стерильный бумажно-пленочный 150x250 мм', 'BAG', NULL, NULL, 'стерильный,пакет,инструменты', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Средний размер для стерилизации', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('bag-sterile-large', 'Пакет стерильный бумажно-пленочный 200x350 мм', 'BAG', NULL, NULL, 'стерильный,пакет,большой,инструменты', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Большой размер для стерилизации', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('bag-biohazard-small', 'Пакет для биоматериалов красный (малый)', 'BAG', NULL, NULL, 'биоопасность,красный,биоматериал', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для утилизации биоматериалов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('bag-biohazard-medium', 'Пакет для биоматериалов красный (средний)', 'BAG', NULL, NULL, 'биоопасность,красный,утилизация', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для утилизации отходов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('bag-biohazard-large', 'Пакет для биоматериалов красный (большой)', 'BAG', NULL, NULL, 'биоопасность,красный,мусор', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для утилизации крупных отходов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('bag-zip-small', 'Зип-пакет прозрачный 100x150 мм', 'ZIP_BAG', NULL, NULL, 'зип,пакет,хранение', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для временного хранения образцов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('bag-zip-medium', 'Зип-пакет прозрачный 150x200 мм', 'ZIP_BAG', NULL, NULL, 'зип,пакет,хранение', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Средний зип-пакет', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('bag-zip-large', 'Зип-пакет прозрачный 200x300 мм', 'ZIP_BAG', NULL, NULL, 'зип,пакет,хранение', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Большой зип-пакет', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('bag-zip-freezer', 'Зип-пакет для заморозки (утолщенный)', 'ZIP_BAG', NULL, NULL, 'зип,заморозка,крио', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Усиленный для низких температур', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('label-thermal-roll', 'Термоэтикетка в рулоне (для принтера)', 'LABEL', NULL, NULL, 'этикетка,термо,принтер', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для лабораторного принтера этикеток', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('label-adhesive-small', 'Самоклеящаяся этикетка 25x50 мм (лист)', 'LABEL', NULL, NULL, 'этикетка,наклейка,маркировка', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для ручной маркировки образцов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('label-adhesive-medium', 'Самоклеящаяся этикетка 50x75 мм (лист)', 'LABEL', NULL, NULL, 'этикетка,наклейка,маркировка', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Средний размер этикетки', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('label-cryo', 'Крио-этикетка (выдерживает -80°C)', 'LABEL', NULL, NULL, 'крио,этикетка,заморозка,морозостойкая', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Специальная для замороженных образцов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tape-scotch-clear', 'Скотч прозрачный канцелярский 19 мм x 33 м', 'TAPE', NULL, NULL, 'скотч,прозрачный,канцелярия', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для упаковки и склеивания', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tape-masking', 'Малярный скотч (для маркировки)', 'TAPE', NULL, NULL, 'малярный,маркировка,временный', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для временной маркировки', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('tape-parafilm', 'Парафильм M (лабораторная пленка) 4 дюйма x 125 футов', 'PARAFILM', NULL, NULL, 'парафильм,гидробарьер,парафин', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для герметизации сосудов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('foil-aluminum-heavy', 'Фольга алюминиевая лабораторная тяжелая', 'FOIL', NULL, NULL, 'фольга,алюминий,защита', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для защиты от света и механических повреждений', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 15: Жидкости и растворы лабораторные (Water, Buffers, Saline)
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('water-distilled-1l', 'Вода дистиллированная 1 л (бидистиллят)', 'DISTILLED_WATER', NULL, NULL, 'вода,дистиллированная,бидистиллят', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для разведения реактивов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('water-distilled-5l', 'Вода дистиллированная 5 л (канистра)', 'DISTILLED_WATER', NULL, NULL, 'вода,дистиллированная,большой объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Большой объем для анализаторов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('water-deionized-1l', 'Вода деионизированная (DI water) 1 л', 'DEIONIZED_WATER', NULL, NULL, 'вода,деионизированная,DI', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для чувствительных анализов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('water-deionized-10l', 'Вода деионизированная (DI water) 10 л', 'DEIONIZED_WATER', NULL, NULL, 'вода,деионизированная,большой объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для анализаторов и систем водоподготовки', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('water-ultrapure-1l', 'Вода ультрачистая (Type I) 1 л', 'DEIONIZED_WATER', NULL, NULL, 'вода,ультрачистая,Type I,HPLC', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Высшая степень очистки для аналитики', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('water-wfi-1l', 'Вода для инъекций (WFI) 1 л', 'DISTILLED_WATER', NULL, NULL, 'вода,WFI,инъекции,стерильная', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Вода для инъекционных растворов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('buffer-pbs-1l', 'Буфер PBS (фосфатно-солевой) 1 л, pH 7.4', 'BUFFER', NULL, NULL, 'PBS,буфер,фосфатный,pH 7.4', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Стандартный фосфатно-солевой буфер', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('buffer-pbs-tablets', 'Таблетки для приготовления PBS (1 таб = 1 л)', 'BUFFER', NULL, NULL, 'PBS,таблетки,концентрат', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Удобный формат для приготовления буфера', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('buffer-tris-1l', 'Буфер Tris-HCl 1 л, pH 8.0', 'BUFFER', NULL, NULL, 'Tris,буфер,pH 8.0', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Tris-буфер для молекулярной биологии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('buffer-tbs-1l', 'Буфер TBS (Tris-buffered saline) 1 л', 'BUFFER', NULL, NULL, 'TBS,буфер,солевой', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Tris-солевой буфер', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('buffer-hepes-100ml', 'Буфер HEPES 100 мл, pH 7.4', 'BUFFER', NULL, NULL, 'HEPES,буфер,клеточная культура', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для клеточной культуры', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('saline-0.9-500ml', 'Физраствор NaCl 0.9% 500 мл (стерильный)', 'SALINE', NULL, NULL, 'физраствор,NaCl,0.9%,стерильный', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Изотонический раствор для инъекций и промываний', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('saline-0.9-1l', 'Физраствор NaCl 0.9% 1 л', 'SALINE', NULL, NULL, 'физраствор,NaCl,0.9%', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Большой объем физраствора', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('saline-0.9-10l', 'Физраствор NaCl 0.9% 10 л (канистра)', 'SALINE', NULL, NULL, 'физраствор,NaCl,0.9%,большой объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для промывания и разведений', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('saline-0.9-bag-500ml', 'Физраствор NaCl 0.9% в мягком пакете 500 мл', 'SALINE', NULL, NULL, 'физраствор,пакет,инфузия', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для инфузионной терапии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gel-ultrasound-250ml', 'Гель для УЗИ 250 мл (прозрачный)', 'GEL', NULL, NULL, 'гель,УЗИ,ультразвук', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Прозрачный гель для ультразвуковых исследований', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('gel-ultrasound-5l', 'Гель для УЗИ 5 л (канистра, экономичный)', 'GEL', NULL, NULL, 'гель,УЗИ,большой объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Большой объем для активного использования', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('detergent-neutral-1l', 'Моющее средство нейтральное для лаборатории 1 л', 'DETERGENT', NULL, NULL, 'моющее,нейтральное,лаборатория', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для мытья лабораторной посуды', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('detergent-alconox-4lb', 'Alconox (порошок для мойки посуды) 4 фунта', 'DETERGENT', NULL, NULL, 'Alconox,порошок,мойка,посуда', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Специализированное моющее для лабораторий', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('paraffin-liquid-500ml', 'Парафин жидкий лабораторный 500 мл', 'PARAFFIN', NULL, NULL, 'парафин,жидкий,гистология', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для гистологии и консервации', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('paraffin-solid-1kg', 'Парафин твердый гистологический 1 кг', 'PARAFFIN', NULL, NULL, 'парафин,твердый,гистология,заливка', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для заливки гистологических блоков', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 16: Фильтры и мембраны (Filters, Syringe filters)
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('filter-paper-qualitative', 'Фильтровальная бумага качественная (средняя скорость)', 'FILTER', NULL, NULL, 'фильтр,бумага,качественная', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для общей фильтрации', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('filter-paper-quantitative', 'Фильтровальная бумага количественная (зольная)', 'FILTER', NULL, NULL, 'фильтр,бумага,количественная,зольная', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для гравиметрического анализа', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('filter-membrane-0.22um', 'Мембранный фильтр 0.22 мкм (стерилизующий)', 'FILTER', NULL, NULL, 'мембрана,0.22,стерилизация,бактерии', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для стерилизующей фильтрации', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('filter-membrane-0.45um', 'Мембранный фильтр 0.45 мкм (стандартный)', 'FILTER', NULL, NULL, 'мембрана,0.45,фильтрация,частицы', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Стандартная фильтрация частиц', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('filter-membrane-nylon', 'Мембранный фильтр нейлон (растворительстойкий)', 'FILTER', NULL, NULL, 'мембрана,нейлон,растворители,органика', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для органических растворителей', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('filter-membrane-pvdf', 'Мембранный фильтр PVDF (низкая адсорбция белков)', 'FILTER', NULL, NULL, 'мембрана,PVDF,белки,низкая адсорбция', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для фильтрации белковых растворов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('syringe-filter-0.22um', 'Шприцевой фильтр 0.22 мкм (PES, стерильный)', 'SYRINGE_FILTER', NULL, NULL, 'шприцевой фильтр,0.22,PES,стерильный', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для стерильной фильтрации малых объемов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('syringe-filter-0.45um', 'Шприцевой фильтр 0.45 мкм (PES)', 'SYRINGE_FILTER', NULL, NULL, 'шприцевой фильтр,0.45,PES', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Стандартный шприцевой фильтр', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('syringe-filter-nylon', 'Шприцевой фильтр нейлон (растворительстойкий)', 'SYRINGE_FILTER', NULL, NULL, 'шприцевой фильтр,нейлон,растворители', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для органических растворителей', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('syringe-filter-ptfe', 'Шприцевой фильтр PTFE (химстойкий)', 'SYRINGE_FILTER', NULL, NULL, 'шприцевой фильтр,PTFE,химстойкий', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для агрессивных химических веществ', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('funnel-buchner-250ml', 'Воронка Бюхнера 250 мл (пластик)', 'FUNNEL', NULL, NULL, 'воронка,Бюхнер,вакуумная фильтрация', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для вакуумной фильтрации', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('funnel-buchner-500ml', 'Воронка Бюхнера 500 мл (пластик)', 'FUNNEL', NULL, NULL, 'воронка,Бюхнер,вакуумная фильтрация', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Большая воронка для вакуумной фильтрации', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('funnel-separating-250ml', 'Воронка делительная 250 мл (стекло/пластик)', 'FUNNEL', NULL, NULL, 'воронка,делительная,экстракция', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для разделения жидкостей', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('funnel-powder', 'Воронка для порошков (широкое горло)', 'FUNNEL', NULL, NULL, 'воронка,порошок,широкое горло', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для пересыпания порошков', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 17: Прочие расходники (Capillaries, Stoppers, Racks, Freezer boxes)
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('capillary-heparinized-20ul', 'Капилляр гематокритный гепаринизированный 20 мкл', 'CAPILLARY', NULL, 'HEMATOLOGY', 'капилляр,гепарин,гематокрит,микрообъем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'С внутренним гепариновым покрытием', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('capillary-plain-20ul', 'Капилляр гематокритный обычный 20 мкл', 'CAPILLARY', NULL, 'HEMATOLOGY', 'капилляр,гематокрит,микрообъем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Стандартный капилляр для капиллярной крови', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('capillary-plain-75ul', 'Капилляр гематокритный обычный 75 мкл', 'CAPILLARY', NULL, 'HEMATOLOGY', 'капилляр,гематокрит,большой объем', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Увеличенный объем капилляра', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('capillary-seal-wax', 'Воск для запечатывания капилляров (гематокрит)', 'CAPILLARY', NULL, 'HEMATOLOGY', 'воск,запечатывание,гематокрит', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для запечатывания одного конца капилляра', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('stopper-rubber-13mm', 'Пробка резиновая 13 мм (для пробирок)', 'STOPPER', NULL, NULL, 'пробка,резиновая,13мм', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Универсальная резиновая пробка', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('stopper-rubber-16mm', 'Пробка резиновая 16 мм (для флаконов)', 'STOPPER', NULL, NULL, 'пробка,резиновая,16мм', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для флаконов и больших пробирок', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('stopper-silicone-13mm', 'Пробка силиконовая 13 мм (химстойкая)', 'STOPPER', NULL, NULL, 'пробка,силиконовая,химстойкая', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Химстойкая силиконовая пробка', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('stopper-screw-cap', 'Заглушка винтовая универсальная', 'STOPPER', NULL, NULL, 'заглушка,винтовая,универсальная', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Винтовая крышка для разных сосудов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('rack-tube-13mm-50pos', 'Штатив для пробирок 13 мм, 50 позиций (пластик)', 'RACK', NULL, NULL, 'штатив,пробирки,13мм,50 позиций', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для малых пробирок', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('rack-tube-16mm-50pos', 'Штатив для пробирок 16 мм, 50 позиций (пластик)', 'RACK', NULL, NULL, 'штатив,пробирки,16мм,50 позиций', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для средних пробирок', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('rack-microtube-1.5ml-96pos', 'Штатив для микропробирок 1.5 мл, 96 позиций', 'RACK', NULL, NULL, 'штатив,микропробирки,Eppendorf,96', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Универсальный штатив для микропробирок', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('rack-microtube-float', 'Плавающий штатив для микропробирок (для водяной бани)', 'RACK', NULL, NULL, 'штатив,плавающий,водяная баня,термостатирование', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для инкубации в водяной бане', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('rack-pcr-0.2ml-96pos', 'Штатив для PCR пробирок 0.2 мл, 96 позиций', 'RACK', NULL, NULL, 'штатив,PCR,0.2мл,96', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для PCR пробирок и стрипов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('rack-serological-15ml-24pos', 'Штатив для серологических пробирок 15 мл', 'RACK', NULL, NULL, 'штатив,серологические,15мл,коническая', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для конических пробирок 15 мл', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('rack-serological-50ml-12pos', 'Штатив для серологических пробирок 50 мл', 'RACK', NULL, NULL, 'штатив,серологические,50мл,коническая', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для конических пробирок 50 мл', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('freezer-box-81pos', 'Криокоробка 81 позиция (для криопробирок 1.8 мл)', 'FREEZER_BOX', NULL, NULL, 'криокоробка,81,заморозка,-80', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Стандартная коробка для -80°C хранилища', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('freezer-box-100pos', 'Криокоробка 100 позиций (для криопробирок)', 'FREEZER_BOX', NULL, NULL, 'криокоробка,100,заморозка', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Увеличенная емкость криокоробки', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('freezer-box-25pos-5ml', 'Криокоробка 25 позиций (для пробирок 5 мл)', 'FREEZER_BOX', NULL, NULL, 'криокоробка,25,5мл,большие пробирки', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для больших криопробирок', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('freezer-box-divider', 'Разделитель для криокоробок (сменный)', 'FREEZER_BOX', NULL, NULL, 'разделитель,криокоробка,сменный', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Сменный разделитель для криокоробок', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Section 18: Оборудование и принадлежности (Printer supplies, Calibrators, QC)
-- =============================================================================

INSERT INTO consumable_inventory (id, name, category, tube_color, linked_analyzer_types, linked_service_keywords, quantity_total, quantity_remaining, unit_price_tenge, lot_number, expiry_date, received_at, received_by, notes, created_at, updated_at, version) VALUES
    ('printer-paper-thermal-57mm', 'Термобумага для принтера 57 мм (диаметр 50 мм)', 'PRINTER_PAPER', NULL, NULL, 'термобумага,принтер,57мм', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для кассовых и лабораторных принтеров', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('printer-paper-thermal-80mm', 'Термобумага для принтера 80 мм (диаметр 80 мм)', 'PRINTER_PAPER', NULL, NULL, 'термобумага,принтер,80мм,широкая', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Широкая термобумага для анализаторов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('printer-paper-thermal-112mm', 'Термобумага для принтера 112 мм (гематология)', 'PRINTER_PAPER', NULL, 'HEMATOLOGY', 'термобумага,гематология,широкая,BC-5000', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для гематологических анализаторов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('printer-paper-z-fold', 'Термобуга Z-fold (сложенная зигзагом)', 'PRINTER_PAPER', NULL, NULL, 'термобумага,Z-fold,сложенная', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Сложенная бумага для автоматической подачи', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('ink-cartridge-black', 'Картридж для принтера (черный)', 'INK_CARTRIDGE', NULL, NULL, 'картридж,черный,принтер,чернила', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Стандартный черный картридж', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('ink-cartridge-color', 'Картридж для принтера (цветной)', 'INK_CARTRIDGE', NULL, NULL, 'картридж,цветной,принтер,чернила', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Цветной картридж', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('toner-cartridge', 'Тонер-картридж для лазерного принтера', 'INK_CARTRIDGE', NULL, NULL, 'тонер,лазерный,принтер', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для лазерных принтеров', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('calibrator-bc5000', 'Калибратор для BC-5000 (Mindray)', 'CALIBRATOR', NULL, 'HEMATOLOGY', 'калибратор,BC-5000,Mindray,гематология', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Оригинальный калибратор для гематологии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('calibrator-bs240', 'Калибратор для BS-240 (Mindray)', 'CALIBRATOR', NULL, 'BIOCHEMISTRY', 'калибратор,BS-240,Mindray,биохимия', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Калибратор для биохимического анализатора', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('qc-hematology-level1', 'Контроль гематологический уровень 1 (норма)', 'QC_MATERIAL', NULL, 'HEMATOLOGY', 'контроль,гематология,норма,level 1', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Нормальный уровень для гематологии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('qc-hematology-level2', 'Контроль гематологический уровень 2 (патология)', 'QC_MATERIAL', NULL, 'HEMATOLOGY', 'контроль,гематология,патология,level 2', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Патологический уровень для гематологии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('qc-hematology-level3', 'Контроль гематологический уровень 3 (нижняя норма)', 'QC_MATERIAL', NULL, 'HEMATOLOGY', 'контроль,гематология,нижняя норма,level 3', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Низкий уровень для гематологии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('qc-biochemistry-level1', 'Контроль биохимический уровень 1 (норма)', 'QC_MATERIAL', NULL, 'BIOCHEMISTRY', 'контроль,биохимия,норма', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Нормальный уровень для биохимии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('qc-biochemistry-level2', 'Контроль биохимический уровень 2 (патология)', 'QC_MATERIAL', NULL, 'BIOCHEMISTRY', 'контроль,биохимия,патология', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Патологический уровень для биохимии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('qc-coagulation', 'Контроль коагулологический (многопараметрический)', 'QC_MATERIAL', NULL, 'COAGULATION', 'контроль,коагулогия,ПТИ,АЧТВ,фибриноген', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для контроля качества коагулологии', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('qc-immunoassay', 'Контроль иммуноферментный (многопараметрический)', 'QC_MATERIAL', NULL, 'IMMUNOASSAY', 'контроль,ИФА,иммунология', 0, 0, NULL, NULL, NULL, CURRENT_DATE, 'system', 'Для контроля иммуноферментных тестов', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- Comments for documentation
-- =============================================================================

COMMENT ON TABLE consumable_inventory IS
    'Laboratory consumables inventory. Comprehensive catalog including tubes, tips, syringes, needles, slides, PPE, cleaning supplies, and analyzer-specific consumables. Quantities, prices, lot numbers, and expiry dates are managed manually after initial seed.';
