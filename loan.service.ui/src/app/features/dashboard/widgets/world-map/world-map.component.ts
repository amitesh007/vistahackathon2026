import { Component, OnInit } from '@angular/core';

interface Region {
  name: string;
  active: boolean;
}

@Component({
  selector: 'app-world-map',
  standalone: false,
  templateUrl: './world-map.component.html',
  styleUrls: ['./world-map.component.scss']
})
export class WorldMapComponent implements OnInit {
  regions: Region[] = [
    { name: 'Europe', active: true },
    { name: 'Asia', active: false },
    { name: 'North America', active: false },
    { name: 'South America', active: false },
    { name: 'Africa', active: false },
    { name: 'Europe', active: false }
  ];

  ngOnInit(): void {
  }

  selectRegion(region: Region): void {
    this.regions.forEach(r => r.active = false);
    region.active = true;
  }
}
