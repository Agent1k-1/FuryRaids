# FuryRaids

Объединённые исходники ERegionPlus и ETNT для Paper 1.21 и Java 21.

```text
src/main/java/com/agent1k/
├── FuryRaids.java
├── api/
├── file/
├── region/
└── tnt/
```

`FuryRaids` — единственный класс JavaPlugin. Он подключает обе системы, регистрирует обработчики и плейсхолдеры, сохраняет данные и перезагружает настройки.

## Файлы настроек

- `region/config.yml` — база, кеш, регионы, частицы и голограммы.
- `region/block.yml` — типы блоков привата, прочность и ремонт.
- `tnt/tnt.yml` — типы TNT, урон ядрам и общие настройки TNT в секции settings.
- `region/messages.yml` — сообщения регионов и значения их плейсхолдеров.
- `tnt/messages.yml` — сообщения TNT.
- `region/menu/menu.yml` — меню региона.
- `data.db` — база созданных регионов в папке `plugins/FuryRaids`.

## Команды и совместимость

Команды `/ps` и `/tnt` поддерживают прежние подкоманды и алиасы. `/ps admin reload` и `/tnt reload` перезагружают настройки обеих систем.

Права `eregionplus.*` и `etnt.*` сохранены. Новые плейсхолдеры: `%region_owner%`, `%region_members%`, `%region_id%`, `%region_type%`, `%region_radius%`, `%region_x_y_z%`, `%region_flag_tnt%`, `%region_owned_count%`. Для TNT: `%tnt_types_count%`, `%tnt_type_name_mini%`, `%tnt_type_radius_mini%`, `%tnt_type_damage_mini%`. Идентификаторы регионов WorldGuard и NBT-ключи предметов и размещённого TNT оставлены совместимыми с исходными плагинами.

При переходе с установленных ERegionPlus и ETNT остановите сервер и сохраните резервные копии. Перенесите базу `data.db` из `plugins/ERegionPlus` в `plugins/FuryRaids` **до первого запуска**, иначе новая база будет пустой, а синхронизация WorldGuard удалит старые управляемые регионы. Файлы регионов перенесите в `plugins/FuryRaids/region`, файлы TNT — в `plugins/FuryRaids/tnt`. У сообщений TNT имя `messages.yml` внутри папки `tnt`. Общие настройки TNT перенесите в секцию `settings` файла `tnt/tnt.yml`. Обновите плейсхолдеры в своих сообщениях, меню и голограммах.

Используйте FuryRaids вместо отдельных ERegionPlus и ETNT. API регионов находится в `com.agent1k.api`; главные классы — `FuryRaidsAPI` и `FuryRaidsProvider`. Внешним плагинам потребуется изменить импорты и зависимость на FuryRaids.

## Зависимости

Из ERegionPlus перенесены WorldGuard 7.0.12, DecentHolograms 2.8.9 и SQLite JDBC 3.44.0.0. Общие зависимости обоих проектов: Paper API 1.21, PlaceholderAPI 2.11.6 и JetBrains Annotations 24.1.0. В `pom.xml` каждая зависимость указана один раз.

WorldGuard обязателен. PlaceholderAPI и DecentHolograms подключаются при наличии. Без DecentHolograms голограммы используют TextDisplay; без PlaceholderAPI плейсхолдеры остаются текстом. SQLite загружается сервером через `libraries` в `plugin.yml`.

## Прочность ядра

`durability` у типа в `region/block.yml` задаёт максимум нового ядра. Значение 0 делает его неуязвимым к взрывам. Начальные значения: 5, 10, 15. В `tnt/tnt.yml` настройка `region-damage` каждого типа задаёт урон за один взрыв: mini — 1, cluster — 2, nuke — 8. Для обычного TNT и вагонетки используется `settings.region-damage`, по умолчанию 1. Урон 0 отключает повреждение ядер этим типом TNT.

Ядро получает урон в пределах четырёх блоков от центра взрыва; если радиус TNT меньше четырёх, используется его радиус. Стены не поглощают урон прочности. Отменённые взрывы и запреты WorldGuard учитываются. Флаг tnt управляет разрушением обычных блоков, а прочность ядра обрабатывается отдельно. При нуле прочности удаляются ядро и регион WorldGuard, предмет ядра не выпадает.

```yaml
repair:
  material: DIAMOND
  restore: 3
  cooldown-after-damage: 60
```

Владелец ремонтирует ядро ПКМ выбранным материалом. Расходуется один предмет и восстанавливается до трёх единиц, не больше максимума. При полной прочности предмет не расходуется. После последнего урона нужно подождать 60 секунд; до окончания этого времени ядро также нельзя забрать киркой. Для обычного открытия меню уберите ремонтный материал из руки.

Прочность, максимум и время последнего повреждения сохраняются в SQLite. Старые базы получают новые столбцы автоматически, старые регионы — начальную прочность своего типа. При снятии и повторной установке состояние сохраняется в предмете. Изменение максимума в конфиге применяется к новым ядрам, у существующих сохраняется записанный максимум.

Плейсхолдеры прочности: `%region_durability%`, `%region_max_durability%`, `%region_durability_percent%`. В голограммах используется собственный регион, даже когда владелец офлайн или находится в другом месте.

## Git и JitPack

Создайте пустой репозиторий FuryRaids в GitHub, замените YOUR_LOGIN своим логином и выполните:

```powershell
Set-Location D:\UPlugin\FuryRaids
git add .
git commit -m "FuryRaids: regions, TNT and core durability"
git branch -M main
git remote add origin https://github.com/YOUR_LOGIN/FuryRaids.git
git push -u origin main
git tag v1.0
git push origin v1.0
```

Откройте https://jitpack.io, найдите YOUR_LOGIN/FuryRaids и выберите Get it для v1.0. jitpack.yml задаёт Java 21 и Maven install без тестов. Успешность удалённой сборки нужно проверить на JitPack.

Подключение в другом Maven-плагине:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>com.github.YOUR_LOGIN</groupId>
        <artifactId>FuryRaids</artifactId>
        <version>v1.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

В plugin.yml использующего API плагина добавьте `depend: [FuryRaids]`.

```java
import com.agent1k.api.FuryRaidsAPI;
import com.agent1k.api.FuryRaidsProvider;

FuryRaidsAPI api = FuryRaidsProvider.get();
```

Модели и события находятся в com.agent1k.api.model и com.agent1k.api.event. У модели Region доступны getDurability(), getMaxDurability() и getLastDamageTime(). Настройка JitPack: https://docs.jitpack.io/building/.
