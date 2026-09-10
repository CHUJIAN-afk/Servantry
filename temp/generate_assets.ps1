Add-Type -AssemblyName System.Drawing

$root = 'D:\IDEA\Summoner\src\main\resources\assets\summoner'
$modelRoot = Join-Path $root 'models'
$paletteDir = Join-Path $root 'textures\palette'
$entityDir = Join-Path $root 'textures\item\entity'
New-Item -ItemType Directory -Force $paletteDir,$entityDir | Out-Null

function New-EmptyBitmap([int]$size) {
    $bmp = New-Object System.Drawing.Bitmap($size, $size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    for ($y = 0; $y -lt $size; $y++) { for ($x = 0; $x -lt $size; $x++) {
        $bmp.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(0, 0, 0, 0))
    } }
    return $bmp
}

function Get-HexColor([string]$hex) {
    $r = [Convert]::ToInt32($hex.Substring(0, 2), 16)
    $g = [Convert]::ToInt32($hex.Substring(2, 2), 16)
    $b = [Convert]::ToInt32($hex.Substring(4, 2), 16)
    return [System.Drawing.Color]::FromArgb(255, $r, $g, $b)
}

function Write-GradientTexture([string]$path, [string]$topHex, [string]$bottomHex) {
    $bmp = New-EmptyBitmap 16
    $top = Get-HexColor $topHex
    $bottom = Get-HexColor $bottomHex
    for ($y = 0; $y -lt 16; $y++) {
        $t = $y / 15.0
        $r = [int](($top.R * (1 - $t)) + ($bottom.R * $t))
        $g = [int](($top.G * (1 - $t)) + ($bottom.G * $t))
        $b = [int](($top.B * (1 - $t)) + ($bottom.B * $t))
        $color = [System.Drawing.Color]::FromArgb(255, [Math]::Min(255, $r), [Math]::Min(255, $g), [Math]::Min(255, $b))
        for ($x = 0; $x -lt 16; $x++) {
            $final = $color
            if ((($x + $y) % 7) -eq 0) {
                $final = [System.Drawing.Color]::FromArgb(255,
                    [Math]::Min(255, $color.R + 12), [Math]::Min(255, $color.G + 12), [Math]::Min(255, $color.B + 12))
            }
            $bmp.SetPixel($x, $y, $final)
        }
    }
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
}

$palettes = @(
    @{ n = 'white';      t = 'f7fbfc'; b = 'b9c6cc' },
    @{ n = 'light_gray'; t = 'e2e5ea'; b = '9da3ae' },
    @{ n = 'gray';       t = '9ba2ad'; b = '5f6672' },
    @{ n = 'dark_gray';  t = '6a7180'; b = '2c3038' },
    @{ n = 'black';      t = '34343c'; b = '0a0a0d' },
    @{ n = 'red';        t = 'ff7a66'; b = 'a32323' },
    @{ n = 'dark_red';   t = 'b83a3a'; b = '541313' },
    @{ n = 'orange';     t = 'ffae5c'; b = 'b84b0f' },
    @{ n = 'yellow';     t = 'ffe576'; b = 'c89a17' },
    @{ n = 'gold';       t = 'ffd75e'; b = 'ad7c16' },
    @{ n = 'lime';       t = 'b5ee61'; b = '629c1b' },
    @{ n = 'green';      t = '7bd854'; b = '2e6f25' },
    @{ n = 'dark_green'; t = '48964a'; b = '1b4a1b' },
    @{ n = 'teal';       t = '58dfb0'; b = '1c6f5d' },
    @{ n = 'cyan';       t = '6fe5f8'; b = '1f86a3' },
    @{ n = 'ice_blue';   t = 'b8ecff'; b = '5e92d1' },
    @{ n = 'blue';       t = '6ba6ff'; b = '244a9e' },
    @{ n = 'dark_blue';  t = '4a66b8'; b = '192557' },
    @{ n = 'purple';     t = 'b084f0'; b = '5b2f9c' },
    @{ n = 'dark_purple';t = '7356b8'; b = '311d61' },
    @{ n = 'magenta';    t = 'f076db'; b = '9a2c85' },
    @{ n = 'brown';      t = 'b47b4e'; b = '583319' },
    @{ n = 'tan';        t = 'e6c999'; b = 'a2763f' },
    @{ n = 'crimson';    t = 'd94b63'; b = '6b1626' },
    @{ n = 'skin';       t = 'ffcf9f'; b = 'b5764d' },
    @{ n = 'wood';       t = 'c49358'; b = '69431f' },
    @{ n = 'steel';      t = 'd9dde4'; b = '818791' },
    @{ n = 'obsidian';   t = '5b5c72'; b = '1d1e2c' },
    @{ n = 'void';       t = '6b65a8'; b = '1b1839' },
    @{ n = 'shadow';     t = '3f3d5e'; b = '14121f' }
)
foreach ($p in $palettes) {
    Write-GradientTexture (Join-Path $paletteDir ($p.n + '.png')) $p.t $p.b
}

$bodyTextures = @{
    'super_peashooter'   = @('8de260', '2f6b22')
    'cloud'              = @('ffffff', 'aebdca')
    'pulse_turret'       = @('aab1bc', '474d58')
    'imp'                = @('d84b3d', '5c1515')
    'necro_spirit'       = @('c2545f', '51131f')
    'barnacle'           = @('82d65c', '275d22')
    'hydra'              = @('57a94c', '1c4718')
    'inferno'            = @('ffa04b', '963b0b')
    'eye_laser_turret'   = @('c3c9d4', '424652')
    'ruin_relic'         = @('ffd963', 'a87615')
    'void_eater_head'    = @('8578c9', '241b4b')
    'void_eater_body'    = @('7568bd', '1f183f')
    'void_eater_tail'    = @('6659aa', '181232')
    'crossbow_bolt'      = @('d2a06a', '63401e')
    'mini_necro_spirit'  = @('e24545', '601018')
    'frost_bolt'         = @('c4f2ff', '4f84c8')
    'explosion_flame_ball' = @('ffbe62', 'bc4a0d')
    'forbidden_orb'      = @('ffe06e', 'a97416')
    'hornet_stinger'     = @('777a88', '1b1c24')
}
foreach ($entry in $bodyTextures.GetEnumerator()) {
    Write-GradientTexture (Join-Path $entityDir ($entry.Key + '.png')) $entry.Value[0] $entry.Value[1]
}

function New-CubeList {
    return ,([System.Collections.Generic.List[object]]::new())
}

function Add-Cube($list, [double]$x1, [double]$y1, [double]$z1,
                  [double]$x2, [double]$y2, [double]$z2, [string]$texture) {
    $dx = [Math]::Abs($x2 - $x1)
    $dy = [Math]::Abs($y2 - $y1)
    $dz = [Math]::Abs($z2 - $z1)
    $faceUv = @{
        north = @(0, 0, $dx, $dy)
        south = @(0, 0, $dx, $dy)
        east  = @(0, 0, $dz, $dy)
        west  = @(0, 0, $dz, $dy)
        up    = @(0, 0, $dx, $dz)
        down  = @(0, 0, $dx, $dz)
    }
    $faces = [ordered]@{}
    foreach ($dir in 'north','south','east','west','up','down') {
        $faces[$dir] = [ordered]@{ uv = $faceUv[$dir]; texture = $texture }
    }
    $list.Add([ordered]@{ from = @($x1, $y1, $z1); to = @($x2, $y2, $z2); faces = $faces }) | Out-Null
}

function Write-Model([string]$folder, [string]$name, [string[]]$accentPalettes, $cubes) {
    $dir = Join-Path $modelRoot $folder
    New-Item -ItemType Directory -Force $dir | Out-Null
    $textures = [ordered]@{}
    $textures['0'] = "summoner:item/entity/$name"
    for ($i = 0; $i -lt $accentPalettes.Count; $i++) {
        $textures[($i + 1).ToString()] = "summoner:palette/$($accentPalettes[$i])"
    }
    $model = [ordered]@{
        format_version = '1.21.11'
        credit         = 'Generated voxel model'
        texture_size   = @(16, 16)
        textures       = $textures
        particle       = "summoner:item/entity/$name"
        elements       = @($cubes)
    }
    $json = $model | ConvertTo-Json -Depth 100
    [System.IO.File]::WriteAllText((Join-Path $dir ($name + '.json')), $json)
}

# Super Peashooter
$c = New-CubeList
Add-Cube $c 4 1 4 12 5 12 '#1'
Add-Cube $c 5 4 5 11 11 11 '#0'
Add-Cube $c 6.5 6.5 11 9.5 9.5 16 '#0'
Add-Cube $c 6.8 6.8 16 9.2 9.2 17.2 '#4'
Add-Cube $c 6.9 7.4 17.2 8.0 8.6 17.4 '#3'
Add-Cube $c 8.0 7.4 17.2 9.1 8.6 17.4 '#3'
Add-Cube $c 4 6 8 4.8 10 12 '#2'
Add-Cube $c 11.2 6 8 12 10 12 '#2'
Add-Cube $c 6.8 11 6.8 9.2 15 9.2 '#2'
Write-Model 'minion' 'super_peashooter' @('dark_green','brown','black','steel') $c

# Cloud
$c = New-CubeList
Add-Cube $c 2 4 4 14 9 12 '#0'
Add-Cube $c 0.8 6 4.5 6 12 9.5 '#0'
Add-Cube $c 10 6 4.5 15.2 12 9.5 '#0'
Add-Cube $c 4.5 8 4 11.5 14.5 9 '#0'
Add-Cube $c 3 2 5 13 6 11 '#1'
Add-Cube $c 6.2 0 7 7.8 3 8.5 '#3'
Add-Cube $c 8.6 0 6.4 10 3 7.8 '#3'
Add-Cube $c 8.2 0.8 8.4 9.6 4.2 9.8 '#3'
Add-Cube $c 8.5 3.5 8.7 9 6.5 9.2 '#4'
Write-Model 'minion' 'cloud' @('gray','dark_gray','ice_blue','yellow') $c

# Pulse Turret
$c = New-CubeList
Add-Cube $c 4 1 4 12 4 12 '#3'
Add-Cube $c 4.5 4 4.5 11.5 11 11.5 '#0'
Add-Cube $c 1 5.5 6 4.5 10.5 10 '#2'
Add-Cube $c 11.5 5.5 6 15 10.5 10 '#2'
Add-Cube $c 6.8 6.5 11 9.2 9.5 15.5 '#2'
Add-Cube $c 6.5 7.4 15.5 9.5 8.6 15.9 '#4'
Add-Cube $c 7 10.5 7 9 14 9 '#1'
Add-Cube $c 7.6 14 7.6 8.4 16.5 8.4 '#1'
Write-Model 'minion' 'pulse_turret' @('dark_gray','dark_blue','cyan','yellow') $c

# Imp
$c = New-CubeList
Add-Cube $c 5 3 5 11 10 11 '#0'
Add-Cube $c 5.5 10 5.5 10.5 15 10.5 '#1'
Add-Cube $c 4.8 13 6 6.3 18 7.2 '#2'
Add-Cube $c 9.7 13 6 11.2 18 7.2 '#2'
Add-Cube $c 1.5 7.5 4 5 12 5.5 '#3'
Add-Cube $c 11 7.5 4 14.5 12 5.5 '#3'
Add-Cube $c 3.8 5.5 6 5 9 7.2 '#1'
Add-Cube $c 11 5.5 6 12.2 9 7.2 '#1'
Add-Cube $c 6.3 11.2 10.4 7.3 12.4 10.8 '#4'
Add-Cube $c 8.7 11.2 10.4 9.7 12.4 10.8 '#4'
Add-Cube $c 6.4 0.5 6.4 9.6 3.5 9.6 '#3'
Write-Model 'minion' 'imp' @('skin','black','dark_purple','yellow') $c

# Necro Spirit
$c = New-CubeList
Add-Cube $c 4 3 4 12 9 12 '#1'
Add-Cube $c 5 8 5 11 15 11 '#0'
Add-Cube $c 3.8 13 3.8 12.2 17.5 12.2 '#2'
Add-Cube $c 5.6 14 12 7.2 15.2 12.4 '#3'
Add-Cube $c 8.8 14 12 10.4 15.2 12.4 '#3'
Add-Cube $c 6 0 6.5 10 4 9.5 '#2'
Write-Model 'minion' 'necro_spirit' @('dark_red','dark_purple','black','red') $c

# Barnacle
$c = New-CubeList
Add-Cube $c 4 2.5 4 12 5 12 '#2'
Add-Cube $c 5 4 5 11 11 11 '#0'
Add-Cube $c 4 6 4 7.5 10 8.5 '#1'
Add-Cube $c 8.5 6 7.5 12 10 12 '#1'
Add-Cube $c 6.8 6.8 11.2 9.2 9.2 11.7 '#3'
Add-Cube $c 5.2 7.5 11.7 6.4 9 16 '#4'
Add-Cube $c 9.6 7.5 11.7 10.8 9 16 '#4'
Add-Cube $c 7.8 6 11.7 8.6 8 16 '#4'
Write-Model 'minion' 'barnacle' @('dark_green','brown','black','dark_red') $c

# Hydra
$c = New-CubeList
Add-Cube $c 3 1 3 13 6 13 '#0'
Add-Cube $c 5 5 5 11 10 11 '#0'
Add-Cube $c 3.8 6.5 6.5 7.2 14 10.5 '#1'
Add-Cube $c 8.8 6.5 6.5 12.2 14 10.5 '#1'
Add-Cube $c 6.4 8 3.5 9.6 15 7 '#1'
Add-Cube $c 3.2 13 6 7.8 16 11 '#1'
Add-Cube $c 8.2 13 6 12.8 16 11 '#1'
Add-Cube $c 6 14 2.5 10 17 7.5 '#1'
Add-Cube $c 4 13.2 10.8 7 14.6 11.3 '#2'
Add-Cube $c 9 13.2 10.8 12 14.6 11.3 '#2'
Add-Cube $c 6.8 14.6 7.2 9.2 16 7.7 '#2'
Write-Model 'minion' 'hydra' @('green','dark_red','black') $c

# Inferno
$c = New-CubeList
Add-Cube $c 4 2 4 12 10 12 '#0'
Add-Cube $c 5.5 8 5.5 10.5 13 10.5 '#1'
Add-Cube $c 3 11 3 6 16 6 '#0'
Add-Cube $c 10 11 10 13 16 13 '#0'
Add-Cube $c 3 11 10 6 16 13 '#0'
Add-Cube $c 10 11 3 13 16 6 '#0'
Add-Cube $c 7 5.5 7 9 9 9 '#2'
Add-Cube $c 7.3 7.3 12 8.7 8.7 12.4 '#3'
Write-Model 'minion' 'inferno' @('yellow','red','dark_red') $c

# Eye Laser Turret
$c = New-CubeList
Add-Cube $c 4 1 4 12 5 12 '#1'
Add-Cube $c 4.5 5 4.5 11.5 11 11.5 '#0'
Add-Cube $c 4 5 4 12 7 12 '#1'
Add-Cube $c 6 6.5 11.4 10 9 12 '#2'
Add-Cube $c 6.5 7 12 9.5 8.5 12.5 '#3'
Add-Cube $c 7.3 7.2 12.5 8.7 8.4 12.9 '#4'
Add-Cube $c 3.2 6 3.2 4.4 11 4.4 '#5'
Add-Cube $c 11.6 6 11.6 12.8 11 12.8 '#5'
Write-Model 'minion' 'eye_laser_turret' @('dark_gray','dark_red','red','black','steel') $c

# Ruin Relic
$c = New-CubeList
Add-Cube $c 6 3 6 10 13 10 '#1'
Add-Cube $c 6.5 3 12 9.5 13 12.6 '#0'
Add-Cube $c 6.5 3 3.4 9.5 13 4 '#0'
Add-Cube $c 12 3 6.5 12.6 13 9.5 '#0'
Add-Cube $c 3.4 3 6.5 4 13 9.5 '#0'
Add-Cube $c 7.2 6 7.2 8.8 9 8.8 '#2'
Add-Cube $c 1.5 6.5 6.8 4 9.5 9.2 '#0'
Add-Cube $c 12 6.5 6.8 14.5 9.5 9.2 '#0'
Write-Model 'minion' 'ruin_relic' @('obsidian','yellow','dark_purple') $c

# Void Eater segments
$c = New-CubeList
Add-Cube $c 4 4 4 12 12 12 '#0'
Add-Cube $c 5 4.8 12 11 11.2 16 '#0'
Add-Cube $c 5 4.8 16.2 11 8 16.6 '#1'
Add-Cube $c 7.2 7.3 16.6 8.8 9 17 '#2'
Add-Cube $c 3.5 3.5 5 4 12.5 9 '#3'
Add-Cube $c 12 3.5 5 12.5 12.5 9 '#3'
Write-Model 'minion' 'void_eater_head' @('black','magenta','dark_purple') $c

$c = New-CubeList
Add-Cube $c 4 4 4 12 12 12 '#0'
Add-Cube $c 3.6 3.6 4.5 4.4 12.4 10.5 '#1'
Add-Cube $c 11.6 3.6 4.5 12.4 12.4 10.5 '#1'
Add-Cube $c 6 3.8 3.6 10 12.2 4.4 '#3'
Write-Model 'minion' 'void_eater_body' @('black','magenta','dark_purple') $c

$c = New-CubeList
Add-Cube $c 5 5 5 11 11 11 '#0'
Add-Cube $c 5.8 5.8 1 10.2 10.2 6 '#0'
Add-Cube $c 6.5 6.5 -2 9.5 9.5 2 '#1'
Add-Cube $c 6.8 6.8 5 9.2 9.2 5.5 '#2'
Write-Model 'minion' 'void_eater_tail' @('black','magenta','dark_purple') $c

# Crossbow bolt
$c = New-CubeList
Add-Cube $c 7.7 7.7 1 8.3 8.3 16 '#0'
Add-Cube $c 7.8 7.8 16 8.2 8.2 19 '#1'
Add-Cube $c 4.6 7.7 0.2 11.4 8.3 4.4 '#2'
Add-Cube $c 7.7 4.6 0.2 8.3 11.4 4.4 '#2'
Add-Cube $c 7.55 7.55 0 8.45 8.45 4 '#3'
Write-Model 'projectile' 'crossbow_bolt' @('steel','tan','black') $c

# Mini necro spirit
$c = New-CubeList
Add-Cube $c 4 4 4 12 12 12 '#0'
Add-Cube $c 5.5 5.5 5.5 10.5 10.5 10.5 '#1'
Add-Cube $c 6.4 6.4 10.5 9.6 9.6 11 '#2'
Add-Cube $c 3.5 3.5 5 4.2 12.5 8 '#3'
Add-Cube $c 11.8 3.5 5 12.5 12.5 8 '#3'
Write-Model 'projectile' 'mini_necro_spirit' @('red','black','dark_purple') $c

# Frost bolt
$c = New-CubeList
Add-Cube $c 7.2 7.2 0 8.8 8.8 15 '#0'
Add-Cube $c 5 6 6 7.5 10 10 '#1'
Add-Cube $c 8.5 6 6 11 10 10 '#1'
Add-Cube $c 6.5 3.5 8 9.5 6 11 '#1'
Add-Cube $c 6.5 10 6 9.5 12.5 9 '#2'
Add-Cube $c 7.6 7.6 15 8.4 8.4 18 '#2'
Write-Model 'projectile' 'frost_bolt' @('light_gray','cyan','white') $c

# Explosion flame ball
$c = New-CubeList
Add-Cube $c 3.5 3.5 3.5 12.5 12.5 12.5 '#0'
Add-Cube $c 5 5 5 11 11 11 '#1'
Add-Cube $c 6.5 6.5 6.5 9.5 9.5 9.5 '#2'
Add-Cube $c 1.5 6 6 3.6 10 10 '#0'
Add-Cube $c 12.4 6 6 14.5 10 10 '#0'
Add-Cube $c 6 1.5 6 10 3.6 10 '#0'
Add-Cube $c 6 12.4 6 10 14.5 10 '#0'
Write-Model 'projectile' 'explosion_flame_ball' @('yellow','red','orange') $c

# Forbidden orb
$c = New-CubeList
Add-Cube $c 5 5 5 11 11 11 '#1'
Add-Cube $c 6.5 6.5 6.5 9.5 9.5 9.5 '#2'
Add-Cube $c 0 7 7 4.5 9 9 '#0'
Add-Cube $c 11.5 7 7 16 9 9 '#0'
Add-Cube $c 7 0 7 9 4.5 9 '#0'
Add-Cube $c 7 11.5 7 9 16 9 '#0'
Add-Cube $c 7 7 0 9 9 4.5 '#0'
Add-Cube $c 7 7 11.5 9 9 16 '#0'
Write-Model 'projectile' 'forbidden_orb' @('black','yellow','gold') $c

# Hornet stinger
$c = New-CubeList
Add-Cube $c 7.7 7.7 0 8.3 8.3 17 '#0'
Add-Cube $c 7.8 7.8 17 8.2 8.2 20 '#2'
Add-Cube $c 4.5 7.7 6 11.5 8.3 6.5 '#1'
Add-Cube $c 7.7 4.5 6 8.3 11.5 6.5 '#1'
Add-Cube $c 7.5 7.5 3.5 8.5 8.5 6 '#3'
Write-Model 'projectile' 'hornet_stinger' @('black','steel','dark_red') $c
